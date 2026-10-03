package com.chinesereads.academy.classroom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chinesereads.academy.access.PremiumGrantQuery;
import com.chinesereads.academy.support.ChineseReadsTokens;
import com.chinesereads.academy.support.MySqlTestSupport;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Corte vertical completo contra MySQL real: alta de profesor → TRIAL (evento) → grupo → código →
 * alumnos se unen → cuota → rol STUDENT → baja → premium_grant (puerto no-op en tests).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "academy.billing.trial-seats=2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClassroomFlowIT extends MySqlTestSupport {

  static final String TEACHER = "teacher@test.local";
  static final String STUDENT_A = "student@test.local";   // id 3
  static final String STUDENT_B = "student2@test.local";  // id 4
  static final String STUDENT_C = "student3@test.local";  // id 5

  static long groupId;
  static String inviteCode;
  static long enrollmentA;

  @Autowired MockMvc mvc;
  @Autowired PremiumGrantQuery grants;

  private static Cookie cookie(String email) {
    return new Cookie("AuthToken", ChineseReadsTokens.access(email));
  }

  @Test
  @Order(1)
  void teacherRegistersAndGetsATrialWithTwoSeats() throws Exception {
    mvc.perform(post("/api/teachers/me").cookie(cookie(TEACHER)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Profe Flow\"}"))
        .andExpect(status().isCreated());

    // El TRIAL lo crea un listener de Modulith tras el commit (asíncrono): esperar a que exista.
    Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
        mvc.perform(get("/api/teacher/seats").cookie(cookie(TEACHER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total", is(2)))
            .andExpect(jsonPath("$.used", is(0)))
            .andExpect(jsonPath("$.subscriptionActive", is(true))));
  }

  @Test
  @Order(2)
  void teacherCreatesAGroupAndAnInviteCode() throws Exception {
    String body = mvc.perform(post("/api/teacher/groups").cookie(cookie(TEACHER)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"HSK2 lunes\",\"level\":\"HSK2\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.activeMembers", is(0)))
        .andReturn().getResponse().getContentAsString();
    groupId = ((Number) JsonPath.read(body, "$.id")).longValue();

    String invite = mvc.perform(post("/api/teacher/groups/" + groupId + "/invite").cookie(cookie(TEACHER))
            .header("X-Requested-With", "XMLHttpRequest"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code", notNullValue()))
        .andReturn().getResponse().getContentAsString();
    inviteCode = JsonPath.read(invite, "$.code");
    assertThat(inviteCode).hasSize(8);

    mvc.perform(get("/api/teacher/groups").cookie(cookie(TEACHER)))
        .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(3)
  void studentsWithoutASeatAreNotStudentsYet() throws Exception {
    mvc.perform(get("/api/me").cookie(cookie(STUDENT_A)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.roles", hasSize(0)));
    mvc.perform(get("/api/student/groups").cookie(cookie(STUDENT_A)))
        .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @Order(4)
  void aStudentJoinsWithTheCodeAndGetsTheStudentRole() throws Exception {
    mvc.perform(post("/api/student/join").cookie(cookie(STUDENT_A)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + inviteCode.toLowerCase() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupId", is((int) groupId)))
        .andExpect(jsonPath("$.alreadyMember", is(false)));

    mvc.perform(get("/api/me").cookie(cookie(STUDENT_A)))
        .andExpect(jsonPath("$.roles", containsInAnyOrder("STUDENT")));
    mvc.perform(get("/api/student/groups").cookie(cookie(STUDENT_A)))
        .andExpect(jsonPath("$[0].name", is("HSK2 lunes")))
        .andExpect(jsonPath("$[0].teacherName", is("Profe Flow")));

    String detail = mvc.perform(get("/api/teacher/groups/" + groupId).cookie(cookie(TEACHER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.members", hasSize(1)))
        .andExpect(jsonPath("$.members[0].name", is("Alumno Test")))
        .andExpect(jsonPath("$.seats.used", is(1)))
        .andExpect(jsonPath("$.activeInvite.uses", is(1)))
        .andReturn().getResponse().getContentAsString();
    enrollmentA = ((Number) JsonPath.read(detail, "$.members[0].enrollmentId")).longValue();
  }

  @Test
  @Order(5)
  void joiningAgainDoesNotConsumeAnotherSeat() throws Exception {
    mvc.perform(post("/api/student/join").cookie(cookie(STUDENT_A)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + inviteCode + "\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.alreadyMember", is(true)));
    mvc.perform(get("/api/teacher/seats").cookie(cookie(TEACHER))).andExpect(jsonPath("$.used", is(1)));
  }

  @Test
  @Order(6)
  void theTeacherCannotJoinTheirOwnGroup() throws Exception {
    mvc.perform(post("/api/student/join").cookie(cookie(TEACHER)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + inviteCode + "\"}"))
        .andExpect(status().isConflict()).andExpect(jsonPath("$.code", is("CLASSROOM_CANNOT_JOIN_OWN_GROUP")));
  }

  @Test
  @Order(7)
  void theSeatQuotaIsEnforced() throws Exception {
    mvc.perform(post("/api/student/join").cookie(cookie(STUDENT_B)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + inviteCode + "\"}"))
        .andExpect(status().isOk());
    mvc.perform(post("/api/student/join").cookie(cookie(STUDENT_C)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + inviteCode + "\"}"))
        .andExpect(status().isConflict()).andExpect(jsonPath("$.code", is("BILLING_SEAT_QUOTA_EXCEEDED")));
    mvc.perform(get("/api/teacher/seats").cookie(cookie(TEACHER))).andExpect(jsonPath("$.used", is(2)));
  }

  @Test
  @Order(8)
  void invalidCodesAreRejected() throws Exception {
    mvc.perform(post("/api/student/join").cookie(cookie(STUDENT_C)).header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"ZZZZZZZZ\"}"))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.code", is("CLASSROOM_INVITE_INVALID")));
  }

  @Test
  @Order(9)
  void deactivatingASeatRemovesTheStudentAndRecordsTheRevoke() throws Exception {
    mvc.perform(delete("/api/teacher/enrollments/" + enrollmentA).cookie(cookie(TEACHER)).header("X-Requested-With", "XMLHttpRequest"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/student/groups").cookie(cookie(STUDENT_A))).andExpect(jsonPath("$", hasSize(0)));
    mvc.perform(get("/api/me").cookie(cookie(STUDENT_A))).andExpect(jsonPath("$.roles", hasSize(0)));
    mvc.perform(get("/api/teacher/seats").cookie(cookie(TEACHER))).andExpect(jsonPath("$.used", is(1)));

    // Los eventos de asiento se procesan tras el commit: GRANT y REVOKE deben quedar DONE (puerto no-op en tests).
    Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      var views = grants.forEnrollment(enrollmentA);
      assertThat(views).extracting(PremiumGrantQuery.GrantView::action).containsExactly("REVOKE", "GRANT");
      assertThat(views).extracting(PremiumGrantQuery.GrantView::status).containsOnly("DONE");
    });
  }

  @Test
  @Order(10)
  void studentsCannotUseTeacherEndpoints() throws Exception {
    mvc.perform(get("/api/teacher/groups").cookie(cookie(STUDENT_B)))
        .andExpect(status().isForbidden()).andExpect(jsonPath("$.code", is("IDENTITY_TEACHER_REQUIRED")));
  }
}
