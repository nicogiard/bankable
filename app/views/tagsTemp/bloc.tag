*{
 *  title (required) titre
 *  class (optional) class attribut of the div element.
 *  #{bloc title:'titre'}
 *     content
 *  #{/bloc}
}*

<div class="bloc ${_class}">

    <div class="title">${_title}</div>
    <div class="content">
      <section>
        
      #{doBody /}
      
      </section>
    </div>
    
</div>