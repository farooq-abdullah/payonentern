package com.learning;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/hello")

public class HelloServlet extends HttpServlet{

   @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {


        response.setContentType("text/plain,charset=UTF-8");
        String name=request.getParameter("name");
        if (name == null || name.isBlank()) {
            name = "Guest";
        }

        PrintWriter writer=response.getWriter();
        writer.write("HEllo"+name);






    }


}





