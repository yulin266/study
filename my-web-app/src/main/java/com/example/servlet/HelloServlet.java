package com.example.servlet;                      // ① 门牌号：声明这个类住在哪个"文件夹"

import jakarta.servlet.ServletException;         // ② Servlet 的"异常类"：init 声明可能抛的错
import jakarta.servlet.annotation.WebServlet;    // ② 注解类：用来给类贴"访问标签"
import jakarta.servlet.http.HttpServlet;         // ② 父类：写 Servlet 必须继承的"模板"
import jakarta.servlet.http.HttpServletRequest;  // ② 请求对象：装浏览器递进来的信息
import jakarta.servlet.http.HttpServletResponse; // ② 响应对象：往浏览器写回内容的出口
import java.io.IOException;                      // ② 输入输出的异常类
import java.io.PrintWriter;                      // ② 字符输出流：往响应里写文字的"笔"
import java.util.Date;                           // ② 日期类：取当前时间用

@WebServlet("/hello")                            // ③ 登记牌：Tomcat 看到它，访问 /hello 就找你
public class HelloServlet extends HttpServlet {  // ④ 定义类：继承父类，白捡一堆现成能力

    @Override                                    // ⑤ 告诉编译器：这是"重写"父类的方法
    public void init() throws ServletException { // ⑤ 上岗：容器创建这个 Servlet 后调一次
        System.out.println("HelloServlet 初始化"); // ⑤ 打印到 IDEA 控制台（生命周期的证据）
    }

    @Override
    protected void doGet(HttpServletRequest req,  // ⑥ 干活：浏览器 GET 访问时，Tomcat 调它
                         HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8"); // ⑥-1 先定"返回格式+编码"，中文不乱码
        PrintWriter out = resp.getWriter();              // ⑥-2 拿"笔"：往响应体里写文字
        out.println("<!DOCTYPE html>");                  // ⑥-3 以下 7 行 = 你写回的网页源码
        out.println("<html><head><title>Hello</title></head><body>");
        out.println("<h1>Hello, Servlet!</h1>");
        out.println("<p>当前时间：" + new Date() + "</p>");
        out.println("<p>请求方法：" + req.getMethod() + "</p>");
        out.println("<p>请求 URI：" + req.getRequestURI() + "</p>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req,   // ⑦ 副手：浏览器 POST 访问时调它
                          HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);                            // ⑦ 偷懒复用：POST 也走 doGet 的逻辑
    }

    @Override
    public void destroy() {                          // ⑧ 下班：关 Tomcat 时调一次
        System.out.println("HelloServlet 销毁");      // ⑧ 打印到控制台
    }
}




