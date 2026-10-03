package com.chinesereads.academy.identity;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chinesereads.academy.support.ChineseReadsTokens;
import com.chinesereads.academy.support.MySqlTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Flujo real contra MySQL: cookie de ChineseReads → /api/me → alta de profesor → rol TEACHER. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MeApiIT extends MySqlTestSupport {

  @Autowired
  MockMvc mvc;

  private static Cookie cookieFor(String email) {
    return new Cookie("AuthToken", ChineseReadsTokens.access(email));
  }

  @Test
  @Order(1)
  void withoutCookieReturnsProblemDetail401() throws Exception {
    mvc.perform(get("/api/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.code", is("UNAUTHENTICATED")))
        .andExpect(jsonPath("$.errorId", matchesPattern("[0-9a-f]{8}")))
        .andExpect(jsonPath("$.correlationId").exists());
  }

  @Test
  @Order(2)
  void withChineseReadsCookieReturnsTheUserReadFromTheSharedTable() throws Exception {
    mvc.perform(get("/api/me").cookie(cookieFor("teacher@test.local")).header("X-Request-Id", "test-correlation-0001"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Request-Id", "test-correlation-0001"))
        .andExpect(jsonPath("$.userId", is(1)))
        .andExpect(jsonPath("$.email", is("teacher@test.local")))
        .andExpect(jsonPath("$.name", is("Profe Test")))
        .andExpect(jsonPath("$.language", is("es")))
        .andExpect(jsonPath("$.roles", hasSize(0)))
        .andExpect(jsonPath("$.teacher", nullValue()));
  }

  @Test
  @Order(3)
  void blockedAccountsStayAnonymous() throws Exception {
    mvc.perform(get("/api/me").cookie(cookieFor("blocked@test.local")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code", is("UNAUTHENTICATED")));
  }

  @Test
  @Order(4)
  void unknownUsersStayAnonymous() throws Exception {
    mvc.perform(get("/api/me").cookie(cookieFor("nobody@test.local")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Order(5)
  void mutationsRequireTheXhrHeader() throws Exception {
    mvc.perform(post("/api/teachers/me").cookie(cookieFor("teacher@test.local"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Profe\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code", is("XHR_HEADER_REQUIRED")));
  }

  @Test
  @Order(6)
  void validationErrorsListTheFields() throws Exception {
    mvc.perform(post("/api/teachers/me").cookie(cookieFor("teacher@test.local"))
            .header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"   \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
        .andExpect(jsonPath("$.errors[0].field", is("displayName")));
  }

  @Test
  @Order(7)
  void registeringAsTeacherGrantsTheTeacherRole() throws Exception {
    mvc.perform(post("/api/teachers/me").cookie(cookieFor("teacher@test.local"))
            .header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Profe Test\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.displayName", is("Profe Test")))
        .andExpect(jsonPath("$.language", is("es")));

    mvc.perform(get("/api/me").cookie(cookieFor("teacher@test.local")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles", containsInAnyOrder("TEACHER")))
        .andExpect(jsonPath("$.teacher.displayName", is("Profe Test")));

    mvc.perform(get("/api/teachers/me").cookie(cookieFor("teacher@test.local")))
        .andExpect(status().isOk());
  }

  @Test
  @Order(8)
  void registeringTwiceIsAConflict() throws Exception {
    mvc.perform(post("/api/teachers/me").cookie(cookieFor("teacher@test.local"))
            .header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Otra vez\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("IDENTITY_TEACHER_ALREADY_REGISTERED")));
  }

  @Test
  @Order(9)
  void studentsCannotUseTeacherEndpoints() throws Exception {
    mvc.perform(get("/api/teachers/me").cookie(cookieFor("student@test.local")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code", is("IDENTITY_TEACHER_REQUIRED")));
  }

  @Test
  @Order(10)
  void unknownRoutesAreNotFoundForAuthenticatedUsers() throws Exception {
    mvc.perform(get("/api/does-not-exist").cookie(cookieFor("teacher@test.local")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("NOT_FOUND")));
  }
}
