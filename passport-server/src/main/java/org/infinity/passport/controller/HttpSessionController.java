package org.infinity.passport.controller;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.infinity.passport.domain.Authority;
import org.infinity.passport.domain.HttpSession;
import org.infinity.passport.exception.NoDataException;
import org.infinity.passport.repository.HttpSessionRepository;
import org.infinity.passport.utils.HttpHeaderCreator;
import org.infinity.passport.utils.PaginationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * REST controller for managing http sessions.
 */
@RestController
@Api(tags = "Http会话")
public class HttpSessionController {

    private static final Logger                LOGGER = LoggerFactory.getLogger(HttpSessionController.class);
    @Autowired
    private              HttpSessionRepository httpSessionRepository;
    @Autowired
    private              HttpHeaderCreator     httpHeaderCreator;

    @ApiOperation("获取Http会话列表")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功获取")})
    @GetMapping("/api/http-session/sessions")
    @Secured({Authority.DEVELOPER})
    @Timed
    public ResponseEntity<List<HttpSession>> find(Pageable pageable,
                                                  @ApiParam(value = "用户名称", required = false) @RequestParam(value = "principal", required = false) String principal) throws URISyntaxException {
        Page<HttpSession> sessions = StringUtils.isEmpty(principal) ? httpSessionRepository.findAll(pageable) : httpSessionRepository.findByPrincipal(pageable, principal);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(sessions, "/api/http-session/sessions");
        return ResponseEntity.ok().headers(headers).body(sessions.getContent());
    }

    @ApiOperation(value = "根据Http会话ID删除Http会话信息", notes = "数据有可能被其他数据所引用，删除之后可能出现一些问题")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "成功删除"), @ApiResponse(code = SC_BAD_REQUEST, message = "Http会话信息不存在")})
    @DeleteMapping("/api/http-session/sessions/{id}")
    @Secured({Authority.DEVELOPER})
    @Timed
    public ResponseEntity<Void> delete(@ApiParam(value = "Http会话ID", required = true) @PathVariable String id) {
        LOGGER.debug("REST request to delete http session: {}", id);
        httpSessionRepository.findById(id).orElseThrow(() -> new NoDataException(id));
        httpSessionRepository.deleteById(id);
        return ResponseEntity.ok().headers(httpHeaderCreator.createSuccessHeader("notification.http.session.deleted", id)).build();
    }
}
