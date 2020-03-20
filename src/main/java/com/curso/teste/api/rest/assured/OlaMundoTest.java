package com.curso.teste.api.rest.assured;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OlaMundoTest {

    @Test
    public void testOlaMundo() {
        Response response = RestAssured.request(Method.GET, "http://restapi.wcaquino.me/ola");
        assertTrue(response.getBody().asString().equals("Ola Mundo!"));
        assertTrue("O status code deveria ser 200",response.getStatusCode() == 200);
        assertEquals(200, response.getStatusCode());

        ValidatableResponse validacao = response.then();
        validacao.statusCode(200);
    }

    @Test
    public void devoConhecerOutrasFormasRestAssured() {
        Response response = RestAssured.request(Method.GET, "http://restapi.wcaquino.me/ola");
        ValidatableResponse validacao = response.then();
        validacao.statusCode(200);

        get("http://restapi.wcaquino.me/ola")
            .then()
            .statusCode(200);

        given()
        .when()
            .get("http://restapi.wcaquino.me/ola")
        .then()
            .statusCode(200);
    }

    @Test
    public void devoConhecerOsMatchersHamcrest() {
        assertThat("Maria", is("Maria"));
        assertThat(128, is(128));
        assertThat(128, Matchers.isA(Integer.class));
        assertThat(128, Matchers.greaterThan(127));

        assertThat("Maria", is((Matchers.not("Joao"))));
        assertThat("Maria", Matchers.not("Joao"));
        assertThat("Maria", Matchers.anyOf(is("Maria"), is("Joaquina")));
        assertThat("Joaquina", Matchers.allOf(Matchers.startsWith("Joa"), Matchers.endsWith("ina"), Matchers.containsString("qui")));
    }

    @Test
    public void devoValidarOBody() {
        given()
        .when()
            .get("http://restapi.wcaquino.me/ola")
        .then()
            .statusCode(200)
            .body(is("Ola Mundo!"))
            .body(containsString("Mundo"))
            .body(is(not(nullValue())));
    }

}
