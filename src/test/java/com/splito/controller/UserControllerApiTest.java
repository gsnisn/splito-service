//package com.splito.controller;
//
//import com.splito.config.TestSecurityConfig;
//import com.splito.dto.mapper.UserMapper;
//import com.splito.model.SplitoUser;
//import com.splito.security.JwtAuthenticationFilter;
//import com.splito.security.JwtService;
//import com.splito.service.UserService;
//import io.restassured.module.mockmvc.RestAssuredMockMvc;
//import io.restassured.http.ContentType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
//import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
//import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
//
//@WebMvcTest(
//        controllers = UserController.class,
//        excludeAutoConfiguration = {
//                HibernateJpaAutoConfiguration.class,
//                DataJpaRepositoriesAutoConfiguration.class,
////                JpaAuditingAutoConfiguration.class,
//                DataSourceAutoConfiguration.class
//        }
//)
//@AutoConfigureMockMvc(addFilters = false)
//class UserControllerApiTest {
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @MockitoBean
//    private UserService userService;
//
//    @MockitoBean
//    private UserMapper userMapper;
//
//    @MockitoBean
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @MockitoBean
//    private JwtService jwtService;
//
//    @BeforeEach
//    void setUp() {
//        RestAssuredMockMvc.webAppContextSetup(context);
//    }
//
//    @Test
//    void me_shouldReturnCurrentUser() {
//        SplitoUser user = new SplitoUser();
//        user.setId(1L);
//        user.setName("Shravan Gupta");
//        user.setEmail("shravan@test.com");
//        user.setPhone("+919876543210");
//
//        when(userService.me()).thenReturn(user);
//
//        String response =
//                given()
//                        .when()
//                        .get("/api/v1/users/me")
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .asString();
//
//        assertThat(response).contains("Shravan Gupta");
//    }
//}