/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engine.chart.api.page;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.emory.mathcs.backport.java.util.Arrays;
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.knowage.slimerjs.wrapper.beans.ViewportDimensions;
import it.eng.knowage.slimerjs.wrapper.enums.RenderFormat;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engine.chart.ChartEngine;
import it.eng.spagobi.engine.chart.ChartEngineInstance;
import it.eng.spagobi.engine.chart.api.AbstractChartEngineResource;
import it.eng.spagobi.engine.chart.api.StyleResource;
import it.eng.spagobi.engine.chart.util.ChartEngineUtil;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors
 *
 */

@Path("/1.0/chart/pages")
public class PageResource extends AbstractChartEngineResource {

	private static final String OUTPUT_TYPE = "outputType";
	private static final String PDF_PAGE_ORIENTATION = "pdfPageOrientation";
	private static final String PDF_ZOOM = "pdfZoomFactor";
	private static final String PDF_WIDTH = "pdfWidth";
	private static final String PDF_HEIGHT = "pdfHeight";
	private static final String PDF_WAIT_TIME = "pdfWaitTime";
	static private final List<String> PDF_PARAMETERS = Arrays
			.asList(new String[] { OUTPUT_TYPE, PDF_WIDTH, PDF_HEIGHT, PDF_WAIT_TIME, PDF_ZOOM, PDF_PAGE_ORIENTATION });
	static private final List<String> JPG_PARAMETERS = Arrays.asList(new String[] { OUTPUT_TYPE });

	static private Map<String, JSONObject> pages;
	static private Map<String, String> urls;

	static private Logger logger = Logger.getLogger(PageResource.class);

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	/**
	 * TODO Tutte le pagine dell'engine
	 *
	 */
	static {
		pages = new HashMap<String, JSONObject>();
		urls = new HashMap<String, String>();

		try {
			pages.put("edit", new JSONObject("{name: 'edit', description: 'the chart edit page', parameters: []}"));
			urls.put("edit", "/WEB-INF/jsp/chart/designer/chartDesigner.jsp");
			pages.put("execute", new JSONObject("{name: 'execute', description: 'the chart execution page', parameters: ['template']}"));

			/* The old (ExtJS) chart execution page (chart.jsp file) is commented, whilst the new one (AngularJS) is used. (danristo) */
			// urls.put("execute", "/WEB-INF/jsp/chart.jsp");
			urls.put("execute", "/WEB-INF/jsp/chart/chartAngular.jsp");

			pages.put("test", new JSONObject("{name: 'test', description: 'the chart test page', parameters: ['template']}"));
			urls.put("test", "/WEB-INF/jsp/test4.jsp");
			pages.put("edit_cockpit", new JSONObject("{name: 'edit_cockpit', description: 'the chart edit page from cockpit', parameters: []}"));
			urls.put("edit_cockpit", "/WEB-INF/jsp/chart/designer/chartDesigner.jsp");
			pages.put("execute_cockpit",
					new JSONObject("{name: 'execute_cockpit', description: 'the chart execution page from cockpit', parameters: ['template']}"));

			/* The old (ExtJS) chart execution page (chart.jsp file) is commented, whilst the new one (AngularJS) is used. (danristo) */
			// urls.put("execute_cockpit", "/WEB-INF/jsp/chart.jsp");
			urls.put("execute_cockpit", "/WEB-INF/jsp/chart/chartAngular.jsp");

		} catch (JSONException t) {
			logger.error(t);
		}
	}

	/**
	 * TODO COMMENTARE
	 *
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY })
	public String getDataSets() {
		try {
			JSONArray resultsJSON = new JSONArray();
			Iterator<String> it = pages.keySet().iterator();
			while (it.hasNext()) {
				String pageName = it.next();
				resultsJSON.put(pages.get(pageName));
			}

			return resultsJSON.toString();
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{pagename}")
	@Produces("text/html")
	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY })
	public void openPage(@PathParam("pagename") String pageName) {
		ChartEngineInstance engineInstance;
		String dispatchUrl = urls.get(pageName);

		try {

			String savedTemplate = getIOManager().getTemplateAsString(pageName.equals("edit"));
			switch (pageName) {

			case "execute":
				String outputType = request.getParameter(OUTPUT_TYPE);
				if ("xls".equals(outputType) || "xlsx".equals(outputType)) {
					request.setAttribute("template", getIOManager().getTemplateAsString());
					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportExcel.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else if ("PDF".equals(outputType)) {
					String requestURL = getRequestUrlForPdfExport(request);
					request.setAttribute("requestURL", requestURL);

					RenderOptions renderOptions = getRenderOptionsForPdfExporter(request);
					request.setAttribute("renderOptions", renderOptions);

					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportPdf.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else if ("JPG".equals(outputType)) {
					String requestURL = getRequestUrlForJpgExport(request);
					request.setAttribute("requestURL", requestURL);

					RenderOptions renderOptions = getRenderOptionsForJpgExporter(request);
					request.setAttribute("renderOptions", renderOptions);

					dispatchUrl = "/WEB-INF/jsp/ngCockpitExportJpg.jsp";
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				} else {
					engineInstance = ChartEngine.createInstance(savedTemplate, getIOManager().getEnv());

					/*
					 * The use of the above commented snippet had led to https://production.eng.it/jira/browse/KNOWAGE-678 and
					 * https://production.eng.it/jira/browse/KNOWAGE-552. The chart engine is stateful, thus the http session is not the place to store and
					 * retrive the engine instance, otherwise concurrency issues are raised.
					 *
					 * @author: Alessandro Portosa (alessandro.portosa@eng.it)
					 */
					// getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
					getExecutionSession().setAttributeInSession(EngineConstants.ENGINE_INSTANCE, engineInstance);

				}

				break;

			case "edit":

				String templateString = savedTemplate != null ? savedTemplate : buildBaseTemplate().toString();

				// create a new engine instance
				engineInstance = ChartEngine.createInstance(templateString, getIOManager().getEnv());

				engineInstance.getEnv().put(EngineConstants.ENV_DOCUMENT_LABEL, getIOManager().getRequest().getParameter("document"));
				/*
				 * The use of the above commented snippet had led to https://production.eng.it/jira/browse/KNOWAGE-678 and
				 * https://production.eng.it/jira/browse/KNOWAGE-552. The chart engine is stateful, thus the http session is not the place to store and retrive
				 * the engine instance, otherwise concurrency issues are raised.
				 *
				 * @author: Alessandro Portosa (alessandro.portosa@eng.it)
				 */
				// getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				getExecutionSession().setAttributeInSession(EngineConstants.ENGINE_INSTANCE, engineInstance);

				/**
				 * These two lines are responsible for setting all chart styles that are available on the server (through their XML files) into the session and
				 * forwarding them to the Designer.js which will take them and put inside of the chart style combo box on the top left of the Designer page.
				 *
				 * @author: atomic (ana.tomic@mht.net)
				 * @commentBy: danristo (danilo.ristovski@mht.net)
				 */
				JSONArray styles = new JSONArray(new StyleResource().getStyles());
				getIOManager().getHttpSession().setAttribute(EngineConstants.DEFAULT_CHART_STYLES, styles);

				break;

			case "test":
				break;

			default:
				dispatchUrl = "/WEB-INF/jsp/error.jsp";
				break;
			}

			// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
			// HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
			// HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

			request.getRequestDispatcher(dispatchUrl).forward(request, response);
		} catch (Exception e) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	private RenderOptions getRenderOptionsForPdfExporter(HttpServletRequest request) throws UnsupportedEncodingException {
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		String encodedUserId = Base64.encode(userId.getBytes("UTF-8"));
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("Authorization", "Direct " + encodedUserId);
		CustomHeaders customHeaders = new CustomHeaders(headers);

		int pdfWidth = Integer.valueOf(request.getParameter(PDF_WIDTH));
		int pdfHeight = Integer.valueOf(request.getParameter(PDF_HEIGHT));
		ViewportDimensions dimensions = new ViewportDimensions(pdfWidth, pdfHeight);

		long pdfRenderingWaitTime = 1000 * Long.valueOf(request.getParameter(PDF_WAIT_TIME));

		RenderOptions renderOptions = RenderOptions.DEFAULT.withCustomHeaders(customHeaders).withDimensions(dimensions)
				.withJavaScriptExecutionDetails(pdfRenderingWaitTime, 5000L);
		return renderOptions;
	}

	private RenderOptions getRenderOptionsForJpgExporter(HttpServletRequest request) throws UnsupportedEncodingException {
		String userId = (String) getUserProfile().getUserUniqueIdentifier();
		String encodedUserId = Base64.encode(userId.getBytes("UTF-8"));
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("Authorization", "Direct " + encodedUserId);
		CustomHeaders customHeaders = new CustomHeaders(headers);
		RenderOptions renderOptions = RenderOptions.DEFAULT.withCustomHeaders(customHeaders).withRenderFormat(RenderFormat.PNG);
		return renderOptions;
	}

	public String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI_SERVICE_JNDI"));
		serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf('/'));
		return serviceURL;
	}

	private String getRequestUrlForPdfExport(HttpServletRequest request) throws UnsupportedEncodingException {
		String requestURL = request.getRequestURL().toString();
		String hostURL = GeneralUtilities.getSpagoBiHost();
		String serviceURL = getServiceHostUrl();
		StringBuilder sb = new StringBuilder(requestURL.replace(hostURL, serviceURL));
		String sep = "?";
		Map<String, String[]> parameterMap = request.getParameterMap();

		for (String parameter : parameterMap.keySet()) {
			if (!PDF_PARAMETERS.contains(parameter)) {
				String[] values = parameterMap.get(parameter);
				if (values != null && values.length > 0) {
					sb.append(sep);
					sb.append(URLEncoder.encode(parameter, "UTF-8"));
					sb.append("=");
					if (parameter.equals(SpagoBIConstants.SBI_HOST)) {
						sb.append(URLEncoder.encode(getServiceHostUrl(), "UTF-8"));
					} else {
						sb.append(URLEncoder.encode(values[0], "UTF-8"));
					}
					sep = "&";
				}
			}
		}
		sb.append("&export=true");
		return sb.toString();
	}

	private String getRequestUrlForJpgExport(HttpServletRequest request) throws UnsupportedEncodingException {
		String requestURL = request.getRequestURL().toString();
		String hostURL = GeneralUtilities.getSpagoBiHost();
		String serviceURL = getServiceHostUrl();
		StringBuilder sb = new StringBuilder(requestURL.replace(hostURL, serviceURL));
		String sep = "?";
		Map<String, String[]> parameterMap = request.getParameterMap();

		for (String parameter : parameterMap.keySet()) {
			if (!JPG_PARAMETERS.contains(parameter)) {
				String[] values = parameterMap.get(parameter);
				if (values != null && values.length > 0) {
					sb.append(sep);
					sb.append(URLEncoder.encode(parameter, "UTF-8"));
					sb.append("=");
					if (parameter.equals(SpagoBIConstants.SBI_HOST)) {
						sb.append(URLEncoder.encode(getServiceHostUrl(), "UTF-8"));
					} else {
						sb.append(URLEncoder.encode(values[0], "UTF-8"));
					}
					sep = "&";
				}
			}
		}
		sb.append("&export=true");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/{pagename}")
	@Produces("text/html")
	@UserConstraint(functionalities = { SpagoBIConstants.CREATE_COCKPIT_FUNCTIONALITY })
	public void openPageFromCockpit(@PathParam("pagename") String pageName, @FormParam("widgetData") String widgetData) {
		ChartEngineInstance engineInstance;
		String dispatchUrl = urls.get(pageName);

		try {

			JSONObject jsonWidgetDataOut = new JSONObject(widgetData);

			Assert.assertTrue(!jsonWidgetDataOut.isNull("widgetData"),
					"It's impossible instantiate a Chart Designer from the Cockpit Engine without a valid [widgetData] param!");

			JSONObject jsonWidgetDataIn = jsonWidgetDataOut.getJSONObject("widgetData");

			String datasetLabel = jsonWidgetDataIn.getString("datasetLabel");
			String chartTemplate;

			if (!jsonWidgetDataIn.isNull("chartTemplate")) {
				chartTemplate = jsonWidgetDataIn.getJSONObject("chartTemplate").toString();
			} else {
				chartTemplate = buildBaseTemplate().toString();
			}

			switch (pageName) {

			case "execute_cockpit":

				engineInstance = ChartEngine.createInstance(chartTemplate, getIOManager().getEnvForWidget());
				engineInstance.getEnv().put(EngineConstants.ENV_DATASET_LABEL, datasetLabel);

				if (!jsonWidgetDataIn.isNull("jsonData") && jsonWidgetDataIn.get("jsonData") != null) {

					String jsonData = null;

					if (jsonWidgetDataIn.get("jsonData") instanceof String) {
						jsonData = jsonWidgetDataIn.getString("jsonData");
					} else if (jsonWidgetDataIn.get("jsonData") instanceof JSONObject) {
						jsonData = jsonWidgetDataIn.getJSONObject("jsonData").toString();
					}

					engineInstance.getEnv().put("METADATA", jsonData);
				}

				if (!jsonWidgetDataIn.isNull("aggregations") && jsonWidgetDataIn.get("aggregations") != null) {

					String aggregations = null;

					if (jsonWidgetDataIn.get("aggregations") instanceof String) {
						aggregations = jsonWidgetDataIn.getString("aggregations");
					} else if (jsonWidgetDataIn.get("aggregations") instanceof JSONObject) {
						aggregations = jsonWidgetDataIn.getJSONObject("aggregations").toString();
					}

					engineInstance.getEnv().put("AGGREGATIONS", aggregations);
				}

				if (!jsonWidgetDataIn.isNull("selections") && jsonWidgetDataIn.get("selections") != null) {
					String selections = null;

					if (jsonWidgetDataIn.get("selections") instanceof String) {
						selections = jsonWidgetDataIn.getString("selections");
					} else if (jsonWidgetDataIn.get("selections") instanceof JSONObject) {
						selections = jsonWidgetDataIn.getJSONObject("selections").toString();
					}

					if (!selections.equals("")) {
						if (!jsonWidgetDataIn.isNull("associations") && jsonWidgetDataIn.get("associations") != null) {
							String associations = null;

							if (jsonWidgetDataIn.get("associations") instanceof String) {
								associations = jsonWidgetDataIn.getString("associations");
							} else if (jsonWidgetDataIn.get("associations") instanceof JSONObject) {
								associations = jsonWidgetDataIn.getJSONObject("associations").toString();
							}

							JSONObject jsonSelections = ChartEngineUtil.cockpitSelectionsFromAssociations(request, selections, associations, datasetLabel);
							Assert.assertNotNull(jsonSelections, "Invalid values for [selections] param");
							engineInstance.getEnv().put("SELECTIONS", jsonSelections.toString());
						} else {
							engineInstance.getEnv().put("SELECTIONS", selections);
						}

					}
				}

				if (!jsonWidgetDataIn.isNull("widgetId") && jsonWidgetDataIn.getString("widgetId") != null) {
					engineInstance.getEnv().put("WIDGETID", jsonWidgetDataIn.getString("widgetId"));
				}

				// engineInstance.getEnv().put("IFRAMEID", getIOManager().getRequest().getParameter("iFrameId"));
				engineInstance.getEnv().put("EXECUTE_COCKPIT", true);

				/*
				 * The use of the above commented snippet had led to https://production.eng.it/jira/browse/KNOWAGE-678 and
				 * https://production.eng.it/jira/browse/KNOWAGE-552. The chart engine is stateful, thus the http session is not the place to store and retrive
				 * the engine instance, otherwise concurrency issues are raised.
				 *
				 * @author: Alessandro Portosa (alessandro.portosa@eng.it)
				 */
				// getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				getExecutionSession().setAttributeInSession(EngineConstants.ENGINE_INSTANCE, engineInstance);
				break;

			case "edit_cockpit":

				// create a new engine instance
				engineInstance = ChartEngine.createInstance(chartTemplate, getIOManager().getEnvForWidget());
				engineInstance.getEnv().put(EngineConstants.ENV_DATASET_LABEL, datasetLabel);
				engineInstance.getEnv().put("EDIT_COCKPIT", true);

				/*
				 * The use of the above commented snippet had led to https://production.eng.it/jira/browse/KNOWAGE-678 and
				 * https://production.eng.it/jira/browse/KNOWAGE-552. The chart engine is stateful, thus the http session is not the place to store and retrive
				 * the engine instance, otherwise concurrency issues are raised.
				 *
				 * @author: Alessandro Portosa (alessandro.portosa@eng.it)
				 */
				// getIOManager().getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
				getExecutionSession().setAttributeInSession(EngineConstants.ENGINE_INSTANCE, engineInstance);

				/**
				 * These two lines are called when the Cockpit engine is initializing (creating, calling) the Designer (not the Chart engine). They are
				 * important for setting all chart types available on the server (inside of XML files) into the session through which we will forward this data
				 * to the Designer.js. This one will use the data and put it inside of the chart style combo box on the top left of the Designer page.
				 *
				 * @author: atomic (ana.tomic@mht.net)
				 * @addedBy: danristo (danilo.ristovski@mht.net)
				 */
				JSONArray styles = new JSONArray(new StyleResource().getStyles());
				getIOManager().getHttpSession().setAttribute(EngineConstants.DEFAULT_CHART_STYLES, styles);

				break;

			default:
				dispatchUrl = "/WEB-INF/jsp/error.jsp";
				break;
			}

			/**
			 * Setting the encoding type to the response object, so the Cockpit engine when calling the rendering of the chart (chart.jsp) can display the real
			 * content of the chart template. If this is not set, specific Italian letters, such as ù and à are going to be displayed as black squared question
			 * marks - they will not be displayed as they are specified by the user.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");

			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
				request.getRequestDispatcher(dispatchUrl).include(request, response);
			} else {
				request.getRequestDispatcher(dispatchUrl).forward(request, response);
			}

		} catch (Exception e) {
			// This causes a java.util.ConcurrentModificationException under cockpit
			// throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), e);
			throw new SpagoBIRuntimeException(e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/executeTest")
	@Produces(MediaType.APPLICATION_JSON)
	public String testAction(@Context HttpServletResponse response) {

		logger.debug("IN");

		try {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			String successString = obj.toString();
			return successString;
		} finally {
			logger.debug("OUT");
		}
	}

	private JSONObject buildBaseTemplate() {
		JSONObject template;

		logger.debug("IN");
		template = new JSONObject();
		logger.debug("OUT");

		return template;
	}
}
