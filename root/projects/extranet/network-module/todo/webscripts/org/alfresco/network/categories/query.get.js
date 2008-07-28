/**

	Incoming payload looks like this
	
	?json=<jsonstring>
	
	json:
	
		{
			"path": "/Releases"
			,
			"categories":
			[
				{ 
					"path" : "/cm:a/cm:b/cm:c"
				}
				,
				{
					"path" : "/d/e/f"
				}
			]
		}
		
		
	?json={"path":"/Releases","categories":[{"path":"/cm:a/cm:b"},{"path":"/cm:d/cm:e"}]}
	
	?json={"path":"/Releases","categories":[{"path":"/categories/General/Releases/ReleaseVersion/2.2"}]}
	
	
	http://localhost:8280/extranet/proxy/alfresco/webframework/query?json=
		{%22path%22:%22/Network/Releases/Published%22,
		%20%22categories%22:[{%20%22path%22%20:%20%22/categories/General/Releases/ReleaseVersion/2.2%22%20},{%20%22path%22%20:%20%22/categories/General/Releases/ReleasePlatform/Windows%22%20},{%20%22path%22%20:%20%22/categories/General/Releases/ReleaseFamily/enterprise%22%20},{%20%22path%22%20:%20%22/categories/General/Releases/ReleaseClass/Enterprise%20Content%20Management%22%20},{%20%22path%22%20:%20%22/categories/General/Releases/ReleaseAssetType/documentation%22%20}]}
	
*/

function collectFiles(file, array)
{
	if(file.isDocument)
	{
		array[array.length] = file;
	}
	else
	{
		if(file.children != null)
		{
			for(var i = 0; i < file.children.length; i++)
			{
				collectFiles(file.children[i], array);
			}
		}
	}
}
	
function filterCategories(rootPath, requiredCategories)
{
	// get all content in this root path
	var node = companyhome.childByNamePath(rootPath);
	var files = new Array();
	collectFiles(node, files);

	var toKeep = new Array();

	// walk over all the files and check against categories
	for(var i = 0; i < files.length; i++)
	{
		var file = files[i];
		
		// create a map of this file's categories
		var fileCatMap = { };
		var fileCategories = file.properties["{http://www.alfresco.org/model/content/1.0}categories"];
		if(fileCategories != null)
		{
			for(var z = 0; z < fileCategories.length; z++)
			{
				var catPath = fileCategories[z].displayPath + "/" + fileCategories[z].name;
				fileCatMap[catPath] = fileCategories[z];
			}
		}
		
		var keep = true;
		
		// walk over the categories we are required to have to have
		if(requiredCategories != null)
		{
			for(var z = 0; z < requiredCategories.length; z++)
			{
				var catPath = requiredCategories[z].path;
				//catPath = "/categories/General" + catPath;
				
				// make sure that this file has that category
				var requiredCategory = fileCatMap[catPath];
				if(requiredCategory == null)
				{
					keep = false;
				}
			}
		}
		
		if(keep)
		{
			toKeep[toKeep.length] = file;
		}		
	}
	
	return toKeep;
}

var json = args["json"];
var jsonObj = eval('(' + json + ')');

var rootPath = jsonObj.path;
var categories = jsonObj.categories;
var results = filterCategories(rootPath, categories);

// store onto model
model.objects = results;
model.includeChildren = true;
model.includeContent = false;

