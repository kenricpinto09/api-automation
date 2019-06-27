package com.company.api.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.company.api.utils.JsonUtils;
import com.company.api.utils.PropertyManager;

import io.restassured.response.Response;

public class AllPlanetsTest {

	private static final Logger LOGGER = LogManager.getLogger();

	private static String url;

	/**
	 * Verify that the endpoint is available
	 */
	@Test
	public void endpointActiveTest() {
		Response response = given().log().all().when().get(url).then().extract().response();
		LOGGER.debug("response body: [{}]", response.asString());
		LOGGER.debug("response code: [{}]", response.getStatusCode());
		// validate the response code
		response.then().statusCode(HttpStatus.SC_OK);
	}

	/**
	 * Method tests the following - url is live - on the first page of the
	 * results, prev value is null - value of total count is an integer - length
	 * of the results array
	 */
	@Test(dependsOnMethods = { "endpointActiveTest" })
	public void planetListTest() {
		Response response = given().log().all().when().get(url).then().extract().response();
		LOGGER.debug("response body: [{}]", response.asString());
		LOGGER.debug("response code: [{}]", response.getStatusCode());

		// validate the response code
		response.then().statusCode(HttpStatus.SC_OK);

		// validate prev value
		String prev = response.then().extract().path("prev");
		Assert.assertNull(prev, "value for 'prev' is not null on the first page");

		// validate and set total planet count
		response.then().body("count", is(instanceOf(Integer.class)));
		int totalCount = response.then().extract().path("count");

		// validate result size
		JSONArray results = JsonUtils.getResultArray(response.asString(), "results");
		LOGGER.debug("results array: [{}]", results);

		if (totalCount >= 10) {
			Assert.assertEquals(results.length(), 10, "result array size should be 10");
		} else if (totalCount > 0) {
			Assert.assertEquals(results.length(), totalCount, "result array size should be " + totalCount);
		} else {
			Assert.fail("result array size is " + results.length());
		}
	}

	/**
	 * Verify that total planet count is equal to the count of planets in the
	 * results array
	 */
	@Test(dependsOnMethods = { "endpointActiveTest" })
	public void planetCountTest() {
		int planetCount = 0;
		String nextUrl = url;
		Response response = null;
		while (true) {
			response = given().log().all().when().get(nextUrl).then().extract().response();

			// validate the response code
			response.then().statusCode(HttpStatus.SC_OK);

			// get planetCount length
			JSONArray results = JsonUtils.getResultArray(response.asString(), "results");
			LOGGER.debug("results array: [{}]", results);
			planetCount = planetCount + results.length();

			// validate if next set of results are available
			nextUrl = response.then().extract().path("next");
			LOGGER.debug("value of next: [{}]", nextUrl);
			if (nextUrl == null) {
				break;
			}
		}
		// validate and set total planet count
		response.then().body("count", is(instanceOf(Integer.class)));
		int totalCount = response.then().extract().path("count");
		LOGGER.debug("total planets: [{}]", totalCount);
		assertEquals(totalCount, planetCount, "total count does not match count of planets from results array");

		// validate prev value
		String next = response.then().extract().path("next");
		Assert.assertNull(next, "value for 'next' is not null on the last page");
	}

	/**
	 * For the first planet in the result array, test that all the urls in the
	 * json are alive
	 */
	@Test(dependsOnMethods = { "endpointActiveTest" })
	public void planetUrlTest() {
		Response response = given().log().all().when().get(url).then().extract().response();
		LOGGER.debug("response body: [{}]", response.asString());
		LOGGER.debug("response code: [{}]", response.getStatusCode());

		// validate the response code
		response.then().statusCode(HttpStatus.SC_OK);

		// validate result
		JSONObject firstPlanetJson = JsonUtils.getResultArray(response.asString(), "results").getJSONObject(0);
		LOGGER.debug("results array: [{}]", firstPlanetJson);

		// validate url is valid
		JSONArray filmsArray = firstPlanetJson.getJSONArray("films");
		for (int i = 0; i < filmsArray.length(); i++) {
			String filmUrl = filmsArray.getString(i);
			given().log().all().get(filmUrl).then().statusCode(HttpStatus.SC_OK);
		}

		JSONArray residentsArray = firstPlanetJson.getJSONArray("residents");
		for (int i = 0; i < residentsArray.length(); i++) {
			String residentUrl = residentsArray.getString(i);
			given().log().all().get(residentUrl).then().statusCode(HttpStatus.SC_OK);
		}

		String planetUrl = firstPlanetJson.getString("url");
		given().log().all().get(planetUrl).then().statusCode(HttpStatus.SC_OK);
	}

	/**
	 * Before class
	 *
	 * @param baseUrl
	 *            the base url
	 */
	@BeforeClass
	@Parameters({ "base-url" })
	public void beforeClass(String baseUrl) {
		String planetsEndpoint = PropertyManager.API_PROPERTIES.getProperty("all-planets");
		Assert.assertNotNull(planetsEndpoint, "value for all-planets from config.properties is null");
		url = baseUrl + planetsEndpoint;
		LOGGER.debug("url: [{}]", url);
	}

}
