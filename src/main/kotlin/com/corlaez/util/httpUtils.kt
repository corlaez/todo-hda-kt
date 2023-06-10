package com.corlaez.util

// Sadly won't fix a checkbox if that checkbox is hx-included.
const val htmxCheckboxFix = """
      window.onload = function() {
        document.body.addEventListener('htmx:configRequest', function(evt) {
          const isInput = evt.detail.elt.tagName == 'INPUT';
          const isCheckbox = evt.detail.elt.type == 'checkbox';
          const isNotChecked = evt.detail.elt.checked == false;
          if(isInput && isCheckbox && isNotChecked) {
            const name = evt.detail.elt.name;
            evt.detail.parameters[name] = "off"; 
          }
        });
      }"""

fun editOnEnterJs(id: Int, filterQueryParam: String): String {
    return """|
|              function editOnEnter (e) {
|                e = e || window.event;
|                console.log(e.target.value);
|                var isEnter = e.key == 'Enter';
|                if (isEnter) {
|                  htmx.ajax(
|                    'PATCH',
|                    '/todo/${id}?${filterQueryParam}',
|                    { target: 'body', values: { content: e.target.value }}
|                  );
|                  e.preventDefault();// Avoids second request onBlur
|                }
|              }"""
}

/** on and null are the browser's default
 * My frontend code ensures false is sent instead of null
 * However null is still possible, as the absence of the value
 * true/false are extensions of mine as it is natural to code that way
 * Any other value will result in a failure */
fun String?.checkboxToBoolean(): Result<Boolean?> = runCatching {
    when(this) {
        "on", "true" -> true
        "off", "false" -> false
        null -> null
        else -> error("Invalid Boolean? value")
    }
}
