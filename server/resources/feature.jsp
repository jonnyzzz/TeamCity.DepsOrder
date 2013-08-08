<%@ include file="/include-internal.jsp"%>
<%--
  ~ Copyright 2000-2013 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="items" scope="request" type="java.util.Collection< jetbrains.teamcity.depsOrder.server.ui.DependencyEntry>" />
<jsp:useBean id="bean" class="jetbrains.teamcity.depsOrder.server.ui.Constants" />

<!-- this page supports .jsp resources resolving -->

<tr>
  <th colspan="2">Select Dependencies Execution Order:</th>
</tr>
<tr>
  <td colspan="2">
    <props:multilineProperty name="${bean.items}" linkTitle="Select Dependencies Order" cols="70" rows="${fn:length(items)+2}" expanded="${true}"/>
  </td>
</tr>

<c:choose>
  <c:when test="${fn:length(items) eq 0}">
    <tr>
      <td colspan="2"><div class="attentionComment">There are no dependencies to reorder</div></td>
    </tr>
  </c:when>
  <c:otherwise>
    <tr>
      <td colspan="2">
        <div class="depsOrderPossibleItems">
          <c:forEach var="it" items="${items}" varStatus="status">
            <c:if test="${not status.first}">, </c:if>
            <a data-build="${it.externalId}" data-ref="${it.reference}"><c:out value="${it.name}"/></a>
          </c:forEach>
        </div>
        <span class="smallNote">Write new-line separated order of dependent build configurations. It's better to use references to make sure those settings survive external IDs rename</span>
      </td>
    </tr>
  </c:otherwise>
</c:choose>

<script type="text/javascript">
  jQuery(function($) {
    $(".depsOrderPossibleItems").on("click", "a", function() {
      var el = $("#${bean.items}");
      el.val(el.val() + "\n" + $(this).data("ref"));
    })
  });
</script>

