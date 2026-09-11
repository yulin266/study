package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 第一个 Servlet
 * 访问路径：http://localhost:8080/untitled/hello
 */
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 设置响应内容类型和字符编码，防止中文乱码
        resp.setContentType("text/html;charset=UTF-8");

        // 读取 URL 参数 name，未提供时用默认值
        String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            name = "World";
        }

        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello, " + name + "!</h1>");
        out.println("<p>当前时间：" + new java.util.Date() + "</p>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp); // 简单复用
    }
}
