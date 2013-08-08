<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="items" scope="request" type="java.util.Collection< jetbrains.teamcity.depsOrder.server.ui.DependencyEntry>" />
<jsp:useBean id="bean" class="jetbrains.teamcity.depsOrder.server.ui.Constants" />

<!-- this page supports .jsp resources resolving -->

<tr>
  <th colspan="2">Select Dependencies Execution Order:</th>
</tr>
<tr>
  <td colspan="2">
    <props:multilineProperty name="${bean.items}" linkTitle="Select Dependencies Order" cols="60" rows="${fn:length(items)+2}" expanded="${true}"/>
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
      </td>
    </tr>
  </c:otherwise>
</c:choose>

<script type="text/javascript">
  jQuery(function($) {
    $(".depsOrderPossibleItems").on("click", "a", function() {
      alert($(this).text());
      var el = $("#${bean.items}");
      el.val(el.val() + "\n" + $(this).data("ref"));
    })
  });
</script>

