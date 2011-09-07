*{
 *  arg (required)
 *  url (required)
 *  pageParamPrefix (optional) Prefix ajouté au paramètre HTTP indiquant le numero de page.
 *  #{pagination pagination, url:actionBridge.Comptes.resume(compte.id) /}
}*
%{
    ( _arg ) && ( _pagination = _arg);
    if(! _pagination) {
        throw new play.exceptions.TagInternalException("pagination attribute cannot be empty for pagination tag");
    }
    if(! _url) {
        throw new play.exceptions.TagInternalException("url attribute cannot be empty for pagination tag");
    }
}%
#{set separator: _url.toString().contains("?") ? "&" : "?" /}
#{set firstPage: _pagination.page - (controllers.utils.Pagination.MAX_PAGE - 1) / 2 /}
#{set lastPage: _pagination.page + (controllers.utils.Pagination.MAX_PAGE - 1) / 2 /}
#{if firstPage < 1 }
  #{set firstDifference: firstPage < 1 ? 1 - firstPage: 0 /}
  #{set firstPage: 1 /}
  #{set lastPage: [lastPage + firstDifference, _pagination.pageCount].min() /}
#{/if}
#{if lastPage > _pagination.pageCount }
  #{set lastDifference: lastPage > _pagination.pageCount ? lastPage - _pagination.pageCount: 0 /}
  #{set lastPage: _pagination.pageCount /}
  #{set firstPage: [firstPage - lastDifference, 1].max() /}
#{/if}

<div class="pagination">
	#{if _pagination.page > 1}<a href="${_url}${separator}${_pageParamPrefix}page=${_pagination.page-1}" class="prev">&laquo;</a>#{/if}
	#{else}<span class="prev">&laquo;</span>#{/else}
	
    
	#{if firstPage > 1 }<span>...</span>#{/if}
	#{list items:firstPage..lastPage, as:'nb'}
	<a href="${_url}${separator}${_pageParamPrefix}page=${nb}" #{if _pagination.page == nb}class="current"#{/if}>${nb}</a>
	#{/list}
    #{if lastPage < _pagination.pageCount }<span>...</span>#{/if}

	#{if _pagination.page < _pagination.pageCount}<a href="${_url}${separator}${_pageParamPrefix}page=${_pagination.page+1}" class="next">&raquo;</a>#{/if}
	#{else}<span class="next">&raquo;</span>#{/else}
</div>
