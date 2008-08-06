<#import "include/globals.ftl" as global />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>${page.title}</title>
	<@global.header/>
	${head}
</head>

<body>
	<div id="custom-doc" class="yui-t6">
	
		<!-- HEADER start -->
		<div id="hd">
			<@region id="header" scope="theme"/>
		</div>
		<!-- HEADER end -->
		
		<!-- NAVIGATION start -->
		<div id="nav">
			<@region id="navigation" scope="global"/>
		</div>
		<!-- NAVIGATION end -->

		<!-- BODY  -->
		<div id="bd">	
			<div id="yui-main">
			
			<@region id="body" scope="page"/>
			
			</div>
		</div>
		<!-- BODY end -->
		
		<!-- FOOTER start -->
		<div id="ft">
			<@region id="footer" scope="global"/>
		</div>
		<!-- FOOTER end -->
		
	</div>

</body>
</html>
