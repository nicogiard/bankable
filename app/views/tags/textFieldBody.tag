*{
 *  create a text field
 *  arg (required) name attribute of the generated textField
 *  label (required) label used for the generated textField
 *  mandatory (optional) is mandatory ?
 *  datepicker (optional) is datePicker ?
 *  calculator (optional) is calculator ?
 *  #{textField 'date', labelProperty:'Date', mandatory:true /}
}*
%{
    ( _arg ) &&  ( _field = _arg);
    if(! _field) {
        throw new play.exceptions.TagInternalException("field attribute cannot be empty for textField tag");
    }
    if(! _label) {
        throw new play.exceptions.TagInternalException("label attribute cannot be empty for textField tag");
    }
}%

<p>
  <label for="${_field.id}">${_label}#{if _mandatory}<span class="mandatory">*</span>#{/if} :</label>
  <input type="text" id="${_field.id}" name="${_field.name}" #{if _datepicker}class="datepicker"#{/if} #{if _calculator}class="calculator"#{/if} value="#{if _datepicker}${_field.value != null ? _field.value.format("dd/MM/yyyy") : ""}#{/if}#{else}${_field.value}#{/else}"/>
  <span class="error">#{error _field.name /}</span>
</p>
