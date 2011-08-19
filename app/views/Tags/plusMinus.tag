*{
 *  arg (required)
 *  #{plusMinus montant /}
}*
%{
    if(_arg == null) {
        throw new play.exceptions.TagInternalException("_arg attribute cannot be empty for pagination tag");
    }
}%
#{if _arg > 0}<img src="@{'/public/images/plus.gif'}" title="Cr&eacute;dit" class="debitCredit">#{/if}
#{elseif _arg < 0}<img src="@{'/public/images/minus.gif'}" title="D&eacute;bit" class="debitCredit">#{/elseif}