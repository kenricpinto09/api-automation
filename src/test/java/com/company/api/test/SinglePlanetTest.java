package com.company.api.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.company.api.utils.JsonUtils;
import com.company.api.utils.PropertyManager;

import io.restassured.response.Response;

public class SinglePlanetTest {

	private static final Logger LOGGER = LogManager.getLogger();

	private static String planetListUrl;

	private static String singlePlanetUrl;

	private static String planetName;
	
	/**
	 * Method tests the following: - single planet url is live - correct planet
	 * is returned - correct data types are used
	 */
	@Test
	public void endpointActiveTest() {
		Response response = given().log().all().when().get(singlePlanetUrl).then().extract().response();
		LOGGER.debug("response body: [{}]", response.asString());
		LOGGER.debug("reponse code: [{}]", response.getStatusCode());
		// validate the response code
		response.then().statusCode(HttpStatus.SC_OK);
	}

	/**
	 * Method tests the following: - single planet url is live - correct planet
	 * is returned - correct data types are used
	 */
	@Test(dependsOnMethods = { "endpointActiveTest" })
	public void planetTest() {
		Response response = given().log().all().when().get(singlePlanetUrl).then().extract().response();
		LOGGER.debug("response body: [{}]", response.asString());
		LOGGER.debug("reponse code: [{}]", response.getStatusCode());

		// validate the response code
		response.then().statusCode(HttpStatus.SC_OK);

		// validate planet name
		Assert.assertEquals(response.then().extract().path("name"), planetName);

		// validate data types
		response.then().body("edited", is(instanceOf(String.class)));
		response.then().body("created", is(instanceOf(String.class)));
		response.then().body("climate", is(instanceOf(String.class)));
		response.then().body("rotation_period", is(instanceOf(String.class)));
		response.then().body("url", is(instanceOf(String.class)));
		response.then().body("population", is(instanceOf(String.class)));
		response.then().body("orbital_period", is(instanceOf(String.class)));
		response.then().body("surface_water", is(instanceOf(String.class)));
		response.then().body("diameter", is(instanceOf(String.class)));
		response.then().body("gravity", is(instanceOf(String.class)));
		response.then().body("name", is(instanceOf(String.class)));
		response.then().body("terrain", is(instanceOf(String.class)));
		response.then().body("films", is(instanceOf(ArrayList.class)));
		response.then().body("residents", is(instanceOf(ArrayList.class)));
	}

	/**
	 * Test that all the urls in the response are live
	 */
	@Test(dependsOnMethods = { "endpointActiveTest" })
	@Parameters({"planet-name"})
	public void planetUrlTest(String planet_name) {
		Response response = given().log().all().when().get(singlePlanetUrl).then().extract().response();
		LOGGER.debug("response body: [{}]", response.asString());
		LOGGER.debug("response code: [{}]", response.getStatusCode());

		// validate the response code
		response.then().statusCode(HttpStatus.SC_OK);

		// validate all urls are valid
		JSONArray filmsArray = JsonUtils.getResultArray(response.getBody().asString(), "films");
		for (int i = 0; i < filmsArray.length(); i++) {
			String filmUrl = filmsArray.getString(i);
			given().log().all().get(filmUrl).then().statusCode(HttpStatus.SC_OK);
		}

		JSONArray residentsArray = JsonUtils.getResultArray(response.getBody().asString(), "residents");
		for (int i = 0; i < residentsArray.length(); i++) {
			String residentUrl = residentsArray.getString(i);
			given().log().all().get(residentUrl).then().statusCode(HttpStatus.SC_OK);
		}

		String planetUrl = response.then().extract().path("url");
		given().log().all().get(planetUrl).then().statusCode(HttpStatus.SC_OK);

	}

	/**
	 * Test that the following are equal - planet data returned by the single
	 * planet endpoint - planet data found using the planet list endpoint
	 */
	@Test(dependsOnMethods = { "endpointActiveTest" })
	public void planetAgainstListTest() {
		Response planetListResponse = given().log().all().when().get(planetListUrl).then().statusCode(HttpStatus.SC_OK)
				.extract().response();
		LOGGER.debug("planet list response body: [{}]", planetListResponse.asString());
		LOGGER.debug("planet list response code: [{}]", planetListResponse.getStatusCode());

		JSONArray planetArray = JsonUtils.getResultArray(planetListResponse.getBody().asString(), "results");
		Boolean isFound = false;
		Boolean hasNext = true;

		JSONObject foundPlanet = null;

		while (!isFound && hasNext) {
			// search for planet in the current array of planets
			for (int i = 0; i < planetArray.length(); i++) {
				JSONObject planet = (JSONObject) planetArray.get(i);
				LOGGER.debug("current planet: [{}]", planet);
				if (planet.getString("name").equalsIgnoreCase(planetName)) {
					isFound = true;
					foundPlanet = planet;
					LOGGER.error("found: [{}]", foundPlanet.toString());
					break;
				}
			}
			if (!isFound) {
				// if planet not found keep looking
				String nextUrl = planetListResponse.then().extract().path("next");
				if (nextUrl != null) {
					planetListResponse = given().log().all().when().get(nextUrl).then().statusCode(HttpStatus.SC_OK)
							.extract().response();
					LOGGER.debug("planet list response body: [{}]", planetListResponse.asString());
					planetArray = JsonUtils.getResultArray(planetListResponse.getBody().asString(), "results");
				} else {
					hasNext = false;
				}
			}
		}

		Response singlePlanetResponse = given().log().all().when().get(singlePlanetUrl).then().extract().response();
		LOGGER.debug("single planet response body: [{}]", singlePlanetResponse.asString());
		LOGGER.debug("single planet response code: [{}]", singlePlanetResponse.getStatusCode());
		JSONObject singlePlanetJson = new JSONObject(singlePlanetResponse.getBody().asString());

		Assert.assertTrue(isFound, "planet: " + planetName + " not found in planet list");
		JSONAssert.assertEquals("found planet and test planet are not equal", foundPlanet, singlePlanetJson,
				JSONCompareMode.NON_EXTENSIBLE);
	}

	/**
	 * Before class.
	 *
	 * @param baseUrl
	 *            the base url
	 * @param planetId
	 *            the planet id
	 * @param planetName
	 *            the planet name
	 */
	@BeforeClass
	@Parameters({ "base-url", "planet-id", "planet-name" })
	public void beforeClass(String baseUrl, String planetId, String planetName) {
		String singlePlanetEndpoint = PropertyManager.API_PROPERTIES.getProperty("planet-by-id");
		Assert.assertNotNull(singlePlanetEndpoint, "value for planet-by-id from config.properties is null");

		singlePlanetUrl = baseUrl + singlePlanetEndpoint.replace("<id>", planetId);
		LOGGER.debug("single planet url: [{}]", singlePlanetUrl);

		this.planetName = planetName;
		LOGGER.debug("planet name: [{}]", planetName);

		String planetsEndpoint = PropertyManager.API_PROPERTIES.getProperty("all-planets");
		Assert.assertNotNull(planetsEndpoint, "value for all-planets from config.properties is null");
		planetListUrl = baseUrl + planetsEndpoint;
	}

}
