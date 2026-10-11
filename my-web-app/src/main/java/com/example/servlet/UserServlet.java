package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/users/*")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            // GET /users  → 列表
            resp.getWriter().println("[{\"id\":1,\"name\":\"张三\"},{\"id\":2,\"name\":\"李四\"}]");
        } else {
            // GET /users/1  → 详情
            String id = pathInfo.substring(1);
            resp.getWriter().println("{\"id\":" + id + ",\"name\":\"用户" + id + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // POST /users → 创建
        resp.setStatus(201);  // Created
        resp.getWriter().println("{\"code\":201,\"msg\":\"创建成功\"}");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        resp.getWriter().println("{\"code\":200,\"msg\":\"更新成功\",\"id\":\""
                + pathInfo.substring(1) + "\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(204);  // No Content
    }
}

