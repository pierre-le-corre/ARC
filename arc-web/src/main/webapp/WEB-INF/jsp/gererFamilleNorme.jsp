<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
	<title><spring:message code="header.familyManagement"/></title>	
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererFamilleNorme.js" />
	</c:import>
</head>

<body class="bg-light">
<form id="selectFamilleNorme"
	action="selectFamilleNorme.action"
	spellcheck="false"
	method="post"
>

	<c:import url="tiles/header.jsp">
		<c:param name="currentPage" value="familyManagement" />
	</c:import>

	<div class="container-fluid">	
	<c:import url="tiles/template_help.jsp">
		<c:param name="helpPage" value="/html/en/selectNormFamily.html" />
	</c:import>
			<div class="row">
				 <div class="col-xl-4 col-sm-12"> 
				 	<div class="row"> 
						<div class="col-xl-12 col-sm-4">
							<c:set var="view" value="${viewFamilleNorme}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="taille" value ="col-md" />
								<c:param name="ligneAdd" value="true" />
								<c:param name="btnSelect" value="true" />
								<c:param name="btnSee" value="true" />
								<c:param name="btnSort" value="true" />
								<c:param name="btnAdd" value="true" />
								<c:param name="btnUpdate" value="true" />
								<c:param name="btnDelete" value="true" />
								<c:param name="checkbox" value="true" />
								<c:param name="checkboxVisible" value="true" />
								<c:param name="extraScopeSee" value="viewClient;viewTableMetier;viewVariableMetier;" />
							</c:import>
						</div>
	
						<div class="col-xl-12 col-sm-4">
							<!-- VIEW TABLE APPLI CLIENTE -->
							<c:set var="view" value="${viewClient}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="taille" value ="col-md" />
								<c:param name="ligneAdd" value="true" />
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="btnAdd" value ="true" />
								<c:param name="btnUpdate" value ="true" />
								<c:param name="btnDelete" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
							</c:import>
						</div>
	
	
						<div class="col-xl-12 col-sm-4">
							<!-- VIEW TABLE METIER -->
							<c:set var="view" value="${viewTableMetier}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="taille" value ="col-md" />
								<c:param name="ligneAdd" value="true" />
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="btnAdd" value ="true" />
								<c:param name="btnUpdate" value ="false" />
								<c:param name="btnDelete" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
								<c:param name="extraScopeAdd" value ="viewVariableMetier;" />
								<c:param name="extraScopeUpdate" value ="viewVariableMetier;" />
								<c:param name="extraScopeDelete" value ="viewVariableMetier;" />
							</c:import>
						</div>
					</div>
				</div>
			<div class="col-xl-8 col-sm-12">
				<div class="col">
						<!-- VIEW VARIABLE METIER -->
						<c:set var="view" value="${viewVariableMetier}" scope="request" />
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="ligneAdd"  value="true" />
							<c:param name="btnSelect" value ="true" />
							<c:param name="btnSee" value ="true" />
							<c:param name="btnSort" value ="true" />
							<c:param name="btnAdd" value ="true" />
							<c:param name="btnUpdate" value ="true" />
							<c:param name="btnDelete" value ="true" />
							<c:param name="checkbox" value ="true" />
							<c:param name="checkboxVisible" value ="true" />
							<c:param name="ligneFilter" value="true" />
							<c:param name="ligneAdd" value ="true" />
							<c:param name="allowResize" value ="true" />
						</c:import>
					</div>
				</div>
			</div>
			</div>
		</form>
</body>
</html>