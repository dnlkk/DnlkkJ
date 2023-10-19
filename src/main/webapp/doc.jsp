<jsp:useBean id="model" scope="request" type="java.lang.Object"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dnlkk API Doc</title>
    <link rel="stylesheet" type="text/css" href="doc.css">
    <script src="doc.js" async></script>
</head>
<body>
<header class="dnlkkdoc main-padding">
    DnlkkDoc
</header>
<nav class="nav-bar nav-bar-panel main-padding">
    <b style="font-size: 30px">DnlkkJ API Doc</b>
    <a href="${model.message}">${model.message}</a>
</nav>
<main class="main-padding">
    <c:forEach var="message" items="${model.controllers}">
        <div>
            <div>
                <div class="gapped-panel">
                    <h2><c:out value="${message.mapping}"/></h2>
                    <h2><c:out value="${message.tagName}"/></h2>
                </div>
                <c:if test="${not empty message.tagDescription}">
                    <b><c:out value="${message.tagDescription}"/></b>
                </c:if>
            </div>
            <br />
            <div class="requests">
                <c:if test="${empty message.requestModels}">
                    <div class="center">
                        <h1>Empty controller</h1>
                    </div>
                </c:if>
                <c:forEach var="requestModel" items="${message.requestModels}">
                    <div class="request accordion accordion__item ${requestModel.requestType}">
                        <div class="gapped-panel accordion__header request-mapping">
                            <button class="request-button ${requestModel.requestType}-button">
                                <c:out value="${requestModel.requestType}"/>
                            </button>
                            <h2><c:out value="${requestModel.mapping}"/></h2>
                        </div>
                        <div class="accordion__body collapse">
                            <hr>
                            <h2><c:out value="${requestModel.apiName}"/></h2>
                            <c:if test="${not empty requestModel.requestParameters}">
                                <h3>Parameters:</h3>
                                <c:forEach var="paramModel" items="${requestModel.requestParameters}">
                                    <pre><c:out value="${paramModel}"/></pre>
                                </c:forEach>
                            </c:if>
                            <c:if test="${not empty requestModel.apiRequest}">
                                <h3>Request:</h3>
                                <pre><c:out value="${requestModel.apiRequest}"/></pre>
                            </c:if>
                            <h3>Response:</h3>
                            <pre><c:out value="${requestModel.apiResponse}"/></pre>
                            <hr>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <hr>
        </div>
    </c:forEach>
</main>
</body>
</html>