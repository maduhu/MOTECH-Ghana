<meta name="heading" content="Patient Registration" />
<h2>Register a Patient</h2>
<form action="${pageContext.request.contextPath}/regTest" method="post">
<table>
	<input type="hidden" name="testAction" value="patient" />
	<tr>
		<td><label for="nursePhone">Nurse Phone:</label></td>
		<td><input name="nursePhone" value="5555555555" /></td>
	</tr>
	<tr>
		<td><label for="serialId">Serial Id:</label></td>
		<td><input name="serialId" value="FGH4894894" /></td>
	</tr>
	<tr>
		<td><label for="name">Name:</label></td>
		<td><input name="name" value="Patient Name" /></td>
	</tr>
	<tr>
		<td><label for="community">Community:</label></td>
		<td><input name="community" value="Community" /></td>
	</tr>
	<tr>
		<td><label for="location">Location:</label></td>
		<td><input name="location" value="Location" /></td>
	</tr>
	<tr>
		<td><label for="nhis">NHIS:</label></td>
		<td><input name="nhis" value="478" /></td>
	</tr>
	<tr>
		<td><label for="patientPhone">Phone:</label></td>
		<td><input name="patientPhone" value="5555555555" /></td>
	</tr>
	<tr>
		<td><label for="dateOfBirth">Date of Birth:</label></td>
		<td><input name="dateOfBirth" value="08/01/1983" /></td>
	</tr>
	<tr>
		<td><label for="gender">Gender:</label></td>
		<td><input name="gender" value="male" /></td>
	</tr>
	<tr>
		<td colspan="2"><input type="submit" /></td>
	</tr>
</table>
</form>