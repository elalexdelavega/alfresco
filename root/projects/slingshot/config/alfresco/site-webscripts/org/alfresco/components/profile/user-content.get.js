

var result = remote.call("/slingshot/profile/usercontents?user=" + stringUtils.urlEncode(page.url.templateArgs["userid"])+"&maxResults=3");

model.addedContent = [];
model.modifiedContent = [];
var numContentToShow = 3;
if (result.status == 200)
{
   // Create javascript objects from the server response
   var data = eval('(' + result + ')');
   ['created','modified'].forEach(function(type){
      var store = (type==='created') ? model.addedContent : model.modifiedContent;
      var content = data[type].items;

      
      for (var i=0,len=content.length;i<len;i++)
      {
         var c = content[i];
         if (store.length < numContentToShow)
         {
            //convert createdOn and modifiedOn fields to date
            if (c[type+'On'])
            {
               c[type+'On'] = new Date(c[type+'On']);
            }
            store.push(c);
         }
      }
      (type==='created') ? model.addedContent = store: model.modifiedContent = store;
   });
   
}
model.numAddedContent = model.addedContent.length;
model.numModifiedContent = model.modifiedContent.length;
