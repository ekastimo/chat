<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CHAT APP</title>
<script type="text/javascript" src="jquery-1.11.3.min.js"></script>
<script type="text/javascript">
	var request = new XMLHttpRequest();
	var running=false;
	function stopXMLHTTP(){
		console.log("On document reload: "+running);
		if(running==true){
			running=false;
			request.abort();					
		}
	}
	$(document).ready(function (){
		console.log("Document Ready Running: "+running);
		$("#login").click(function go(){
			running=true;
			console.log("Calling go() ");
			var url = "times/WeatherServlet?username=" + $("#username").val();
			console.log("URL: " + url);
			request.open("GET", url, true);
			request.setRequestHeader("Content-Type", "application/x-javascript;");
			request.onreadystatechange = function() {
				/*
				readyState has four different states : 
				0: request not initialized 
				1: server connection established
				2: request received 
				3: processing request 
				4: request finished and response is ready
				status is ranging between 200 - Ok and 404 - Page Not Found     
				 */
				console.log("readyState " + request.readyState);
				if (request.readyState == 4) {
					console.log("status " + request.status);
					if (request.status == 200) {
						if (request.responseText) {
							$("<p>" + request.responseText + "</p>").appendTo(
									"#messages");
						}
					}
					console.log("Running " + running);
					if(running==true)
						go();
				}
			};
			request.send(null);
		});
		$("#send").click(function() {
			console.log("clicked btn");
			$.post("times/WeatherServlet", {
				username : $("#username").val(),
				message : $("#message").val()
			}, function(data, status) {
				console.log("Status: "+ status );
			});
		});
	});
</script>
<style type="text/css">
.myDiv{	
	border: 1px solid #000000;
	background-color: #eeffff;
	width: 100%;
	height: 100%;
}
</style>
</head>
<body onbeforeunload="return stopXMLHTTP()">
	<table width="50%" height="100%">
		<tr>
			<td><h3 align="center">Chat App</h3></td>
		</tr>
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td width="30%">Name</td>
						<td width="70%">
						<input type="text" name="username" id="username" /> 
						<input type="button"  name="login" id="login" value="LOGIN" /></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td width="30%">Message</td>
						<td width="70%"><input type="text" name="message" id="message" /> 
						<input type="submit" name="send" id="send" value="SEND" /></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td height="100%" width="100%" > <div class="myDiv" id="messages"></div></td>
		</tr>
	</table>
</body>
</html>