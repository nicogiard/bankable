*{
 *  arg (required)
 *  url (required)
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
<div class="pagination">
	#{if _pagination.page > 1}<a href="${_url}?page=${_pagination.page-1}" class="prev">&laquo;</a>#{/if}
	#{else}<span class="prev">&laquo;</span>#{/else}
	#{list items:1.._pagination.pageCount, as:'nb'}
	<a href="${_url}?page=${nb}" #{if _pagination.page == nb}class="current"#{/if}>${nb}</a>
	#{/list}
	#{if _pagination.page < _pagination.pageCount}<a href="${_url}?page=${_pagination.page+1}" class="next">&raquo;</a>#{/if}
	#{else}<span class="next">&raquo;</span>#{/else}
</div>
