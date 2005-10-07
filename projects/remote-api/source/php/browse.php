<?php

   require_once('alfresco/Types.php');
   require_once('alfresco/RepositoryService.php');
   require_once('alfresco/ContentService.php');

   $store = new Store('SpacesStore');
   $reference = null;
   $path = null;

   $repository_service = new RepositoryService($_REQUEST['username'], $_REQUEST['ticket']);
   $content_service = new ContentService($_REQUEST['username'], $_REQUEST['ticket']);

   if (isset($_REQUEST['uuid']) == false)
   {
      $reference = new Reference($store, null, "//*[@cm:name=\"Company Home\"]");
      $path = 'Company Home';

   }
   else
   {
      $reference = new Reference($store, $_REQUEST['uuid']);
      $path = $_REQUEST['path'].'|'.$_REQUEST['uuid'].'|'.$_REQUEST['name'];
   }

   $queryResults = $repository_service->queryChildren($reference);
   
   function getValue($columns, $prop_name)
   {
      $value = null;
      foreach($columns as $column)
      {
         if ($column->name == $prop_name)
         {
            $value = $column->value;
         }
      }
      return $value;
   }

   function getURL($current_id, $current_name, $path, $current_type="{http://www.alfresco.org/model/content/1.0}folder")
   {
      global $store, $content_service;

      $result = null;
      if ($current_type == "{http://www.alfresco.org/model/content/1.0}content")
      {
         $read_result = $content_service->read(new Reference($store, $current_id));
         $result = $read_result->url."?ticket=".$_REQUEST['ticket'];
      }
      else
      {
         $result = "browse.php?username=".$_REQUEST['username']."&ticket=".$_REQUEST['ticket']."&uuid=".$current_id."&name=".$current_name."&path=".$path;
      }

      return $result;
   }

   function outputTR($current_id, $current_type, $rows)
   {
       global $path;

       print('<tr>');
       if (isset($rows->columns))
       {
          $current_name = getValue($rows->columns, '{http://www.alfresco.org/model/content/1.0}name');

          print('<td>');

          print("<a href='");
          print(getURL($current_id, $current_name, $path, $current_type));
          print("'>");
          print($current_name);
          print("</a>");

          print('</td>');
       }
       else
       {
          print '<td>&nbsp;</td>';
       }
       print('</tr>');
   }
   
   function outputTable($title, $query_results, $type_filter, $empty_message)
   {
       if (isset($query_results->resultSet->size) && $query_results->resultSet->size == 0)
       {
          print $empty_message;
       }
       else
       {
          print(
                '<table border="0" width="95%" align="center">'.
                '   <tr style="{background-color: #D3E6FE}">'.
                '      <td>'.$title.'</td>'.
                '   </tr>'.
                '   <tr>'.
                '      <td>'.
                '         <table border="0" width="100%">');

          if (isset($query_results->resultSet->rows))
          {
             $row = $query_results->resultSet->rows;
             $current_id = $row->node->id;
             $current_type = $row->node->type;
             if ($current_type == $type_filter)
             {
                outputTR($current_id, $current_type, $row);
             }
          }
          else
          {
            for ($i = 0; $i < count($query_results->resultSet)-1; $i++)
            {
               $current_id = $query_results->resultSet[$i]->node->id;
               $current_type = $query_results->resultSet[$i]->node->type;
               if ($current_type == $type_filter)
               {
                  outputTR($current_id, $current_type, $query_results->resultSet[$i]);
               }
             }
          }

          print(
                '         </table>'.
                '      </td>'.
                '   </tr>'.
                '</table>');
       }
   }
   
   function outputBreadcrumb($path)
   {
      print(
          '<table border="0" width="95%" align="center">'.
          '   <tr>'.
          '      <td>');

      $values = split("\|", $path);
      $home = $values[0];
      $path = $home;
      $id_map = array();
      for ($counter = 1; $counter < count($values); $counter += 2)
      {
         $id_map[$values[$counter]] = $values[$counter+1];
      }

       print("<a href='browse.php?username=".$_REQUEST['username']."&ticket=".$_REQUEST['ticket']."'><b>".$home."</b></a>");
       foreach($id_map as $id=>$name)
       {
          $path .= '|'.$id.'|'.$name;
          print("&nbsp;&gt;&nbsp;<a href='".getURL($id, $name, $path)."'><b>".$name."</b></a>");
       }

       print(
        '      </td>'.
        '   </tr>'.
        '</table>');
   }

?>

<html>
   <head>
      <title>Browse Repository</title>
      <style>
         body {font-family: verdana; font-size: 8pt;}
         tr {font-family: verdana; font-size: 8pt;}
         td {font-family: verdana; font-size: 8pt;}
         input {font-family: verdana; font-size: 8pt;}
         .maintitle {font-family: verdana; font-size: 10pt; font-weight: bold; padding-bottom: 15px;}
         a:link, a:visited
         {
      	 font-size: 11px;
      	 color: #465F7D;
      	 text-decoration: none;
      	 font-family: Tahoma, Arial, Helvetica, sans-serif;
      	 font-weight: normal;
        }
        a:hover
        {
        	color: #4272B4;
        	text-decoration: underline;
        	font-weight: normal;
        }
      </style>
   </head>

   <body>
<?php

   if (PEAR::isError($queryResults))
   {
      print("<span style='font-weight:bold;color:red'>Error occurred: ");

      if ($queryResults->getMessage())
      {
         print($queryResults->getMessage());
      }
      else
      {
         print($queryResults->userinfo->RepositoryFault->message);
      }
      
      print("</span>\n");
   }
   else
   {
       outputBreadcrumb($path);
       outputTable("Browse Spaces", $queryResults, "{http://www.alfresco.org/model/content/1.0}folder", "There are no spaces");
       outputTable("Content items", $queryResults, "{http://www.alfresco.org/model/content/1.0}content", "There is no content");
   }
?>

   </body>

</html>