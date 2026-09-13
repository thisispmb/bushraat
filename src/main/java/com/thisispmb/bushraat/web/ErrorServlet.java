package com.thisispmb.bushraat.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/error")
public class ErrorServlet extends ThymeleafServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        renderError(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        renderError(request, response);
    }

    private void renderError(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Object statusValue =
                request.getAttribute("jakarta.servlet.error.status_code");

        int status = statusValue instanceof Number
                ? ((Number) statusValue).intValue()
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        HttpSession session = request.getSession(false);

        boolean loggedIn =
                session != null && session.getAttribute("user") != null;

        ErrorDetails details = getErrorDetails(
                status,
                loggedIn,
                request.getContextPath()
        );

        response.setStatus(status);
        response.setContentType("text/html;charset=UTF-8");

        request.setAttribute("errorCode", status);
        request.setAttribute("errorTitle", details.title());
        request.setAttribute("errorMessage", details.message());
        request.setAttribute("errorPrimaryText", details.primaryText());
        request.setAttribute("errorPrimaryUrl", details.primaryUrl());
        request.setAttribute("errorSecondaryText", details.secondaryText());
        request.setAttribute("errorSecondaryUrl", details.secondaryUrl());

        try {
            render("error", request, response);
        } catch (Exception ignored) {
            response.resetBuffer();
            response.setStatus(status);
            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().printf(
                    "<!doctype html>" +
                            "<html>" +
                            "<head>" +
                            "<meta charset=\"UTF-8\">" +
                            "<title>%d - Bushraat</title>" +
                            "</head>" +
                            "<body>" +
                            "<h1>%d</h1>" +
                            "<h2>%s</h2>" +
                            "<p>%s</p>" +
                            "<a href=\"%s\">%s</a>" +
                            "<a href=\"%s\">%s</a>" +
                            "</body>" +
                            "</html>",
                    status,
                    status,
                    escape(details.title()),
                    escape(details.message()),
                    escape(details.primaryUrl()),
                    escape(details.primaryText()),
                    escape(details.secondaryUrl()),
                    escape(details.secondaryText())
            );
        }
    }

    private ErrorDetails getErrorDetails(
            int status,
            boolean loggedIn,
            String contextPath
    ) {

        String dashboard = contextPath + "/dashboard";
        String home = contextPath + "/";
        String back = "javascript:history.back()";

        return switch (status) {

            case 400 -> new ErrorDetails(
                    "Bad request",
                    "The request could not be understood. Please check the information you provided.",
                    "Try again",
                    back,
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home
            );

            case 403 -> new ErrorDetails(
                    "Access denied",
                    "You don't have permission to access this resource.",
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home,
                    "Go back",
                    back
            );

            case 404 -> new ErrorDetails(
                    "Page not found",
                    "We couldn't find the page or resource you're looking for.",
                    loggedIn ? "Go to dashboard" : "Back home",
                    loggedIn ? dashboard : home,
                    "Go back",
                    back
            );

            case 405 -> new ErrorDetails(
                    "Method not allowed",
                    "This action isn't supported for this resource.",
                    "Go back",
                    back,
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home
            );

            case 413 -> new ErrorDetails(
                    "File too large",
                    "The file is too large to be uploaded.",
                    "Go back",
                    back,
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home
            );

            case 415 -> new ErrorDetails(
                    "Unsupported file type",
                    "This file type isn't supported.",
                    "Go back",
                    back,
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home
            );

            case 500 -> new ErrorDetails(
                    "Something went wrong",
                    "We couldn't complete your request. Please try again.",
                    "Try again",
                    back,
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home
            );

            default -> new ErrorDetails(
                    "Something went wrong",
                    "We couldn't complete your request.",
                    "Try again",
                    back,
                    loggedIn ? "Go to dashboard" : "Go home",
                    loggedIn ? dashboard : home
            );
        };
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record ErrorDetails(
            String title,
            String message,
            String primaryText,
            String primaryUrl,
            String secondaryText,
            String secondaryUrl
    ) {
    }
}