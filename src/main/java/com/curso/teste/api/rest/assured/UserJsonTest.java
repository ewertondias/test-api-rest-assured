package com.curso.teste.api.rest.assured;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.request;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserJsonTest {

    public static RequestSpecification requestSpecification;
    public static ResponseSpecification responseSpecification;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://restapi.wcaquino.me";

        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.log(LogDetail.ALL);
        requestSpecification = requestSpecBuilder.build();

        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        responseSpecBuilder.expectStatusCode(200);
        responseSpecification = responseSpecBuilder.build();

        // Colocando no rest assured não precisa especificar nos métodos vao pegar automaticamente
        // RestAssured.requestSpecification = requestSpecification;
        // RestAssured.responseSpecification = responseSpecification;
    }

    @Test
    public void deveVerificarPrimeiroNivel() {
        given()
            .spec(requestSpecification)
        .when()
            .get("/users/1")
        .then()
            // .statusCode(200)
            .spec(responseSpecification)
            .body("id", is(1))
            .body("name", containsString("Silva"))
            .body("age", greaterThan(18));
    }

    @Test
    public void deveVerificarPrimeiroNivelOutrasFormas() {
        Response response = request(Method.GET, "/users/1");

        // Path
        assertEquals(Integer.valueOf(1), response.path("id"));

        // JsonPath
        JsonPath jsonPath = new JsonPath(response.asString());
        assertEquals(1, jsonPath.getInt("id"));

        // From
        int id = JsonPath.from(response.asString()).getInt("id");
        assertEquals(1, id);
    }

    @Test
    public void deveVerificarSegundoNivel() {
        given()
        .when()
            .spec(requestSpecification)
            .get("/users/2")
        .then()
            // .statusCode(200)
            .spec(responseSpecification)
            .body("id", is(2))
            .body("name", containsString("Joaquina"))
            .body("endereco.rua", is("Rua dos bobos"));
    }

    @Test
    public void deveVerificarLista() {
        given()
        .when()
            .get("/users/3")
        .then()
            .statusCode(200)
            .body("id", is(3))
            .body("name", containsString("Ana"))
            .body("filhos", hasSize(2))
            .body("filhos[0].name", is("Zezinho"))
            .body("filhos[1].name", is("Luizinho"))
            .body("filhos.name", hasItem("Zezinho"))
            .body("filhos.name", hasItems("Zezinho", "Luizinho"));
    }

    @Test
    public void deveRetornarErroUsuarioInexistente() {
        given()
        .when()
            .get("/users/4")
        .then()
            .statusCode(404)
            .body("error", is("Usuário inexistente"));
    }

    @Test
    public void deveVerificarListaNaRaiz() {
        given()
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("", hasSize(3))
            .body("name", hasItems("João da Silva", "Maria Joaquina", "Ana Júlia"))
            .body("age[1]", is(25))
            .body("filhos.name", hasItem(Arrays.asList("Zezinho", "Luizinho")))
            .body("salary", contains(1234.5678f, 2500, null));
    }

    /**
     * As anotações dentro do body são baseadas no groovy
     */
    @Test
    public void devoFazerVerificacoesAvancadas() {
        given()
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("age.findAll{it <= 25}.size()", is(2))
            .body("age.findAll{it <= 25 && it > 20}.size()", is(1))
            .body("findAll{it.age <= 25 && it.age > 20}.name", hasItem("Maria Joaquina"))
            .body("findAll{it.age <= 25}[0].name", is("Maria Joaquina"))
            .body("findAll{it.age <= 25}[-1].name", is("Ana Júlia"))
            .body("find{it.age <= 25}.name", is("Maria Joaquina"))
            .body("findAll{it.name.contains('n')}.name", hasItems("Maria Joaquina", "Ana Júlia"))
            .body("findAll{it.name.length() > 10}.name", hasItems("João da Silva", "Maria Joaquina"))
            .body("name.collect{it.toUpperCase()}", hasItem("MARIA JOAQUINA"))
            .body("name.findAll{it.startsWith('Maria')}.collect{it.toUpperCase()}", hasItem("MARIA JOAQUINA"))
            .body("name.findAll{it.startsWith('Maria')}.collect{it.toUpperCase()}.toArray()",
                allOf(arrayContaining("MARIA JOAQUINA"), arrayWithSize(1)))
            .body("age.collect{it * 2}", hasItems(60, 50, 40))
            .body("id.max()", is(3))
            .body("salary.min()", is(1234.5678f))
            .body("salary.findAll{it != null}.sum()", is(closeTo(3734.5678f, 0.001)))
            .body("salary.findAll{it != null}.sum()", allOf(greaterThan(3000d), lessThan(5000d)));
    }

    @Test
    public void devoUnirJsonPathComJava() {
        ArrayList<String> names =
            given()
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .extract().path("name.findAll{it.startsWith('Maria')}");

        assertEquals(1, names.size());
        assertTrue(names.get(0).equalsIgnoreCase("Maria Joaquina"));
        assertEquals(names.get(0).toUpperCase(), "Maria Joaquina".toUpperCase());
    }

}
