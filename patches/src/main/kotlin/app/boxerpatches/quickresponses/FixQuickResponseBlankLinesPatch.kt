package app.boxerpatches.quickresponses

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val fixQuickResponseBlankLinesPatch = resourcePatch(
    name = "Fix Boxer Quick Response blank lines",
    description = "Preserves paragraph and empty-line formatting when inserting Quick Responses in Workspace ONE Boxer.",
) {
    compatibleWith("com.boxer.email")

    execute {
        val selectionJs = get("assets/selection.js")
        val source = selectionJs.readText()

        // If the file already contains our implementation, do nothing.
        if (source.contains("boxerQuickResponseNewlineFix")) {
            return@execute
        }

        // Replace the complete insertText() implementation rather than matching an
        // exact whitespace/comment block. Boxer has shipped the same function with
        // small formatting differences, which made the first patch unnecessarily brittle.
        // We anchor the match to the following insertHtml() function so we cannot
        // accidentally replace an unrelated block.
        val insertTextFunction = Regex(
            pattern = """(?s)function\s+insertText\s*\(\s*text\s*\)\s*\{.*?\n\}\s*\n\s*function\s+insertHtml\s*\("""
        )

        val replacement = """
function insertText(text) {
  const boxerQuickResponseNewlineFix = true;
  const selection = window.getSelection();
  let range;

  if (!selection.rangeCount) {
    const el = getRootElement();
    if (!el) return;
    range = document.createRange();
    range.setStart(el, 0);
  } else {
    range = selection.getRangeAt(0);
  }

  range.deleteContents();

  // The composer uses white-space: normal. Newline characters in one text node
  // therefore collapse. Insert real BR nodes so \n\n remains an empty line.
  const normalizedText = String(text).replace(/\r\n?/g, "\n");
  const lines = normalizedText.split("\n");
  const fragment = document.createDocumentFragment();
  let lastNode = null;

  lines.forEach((line, index) => {
    if (index > 0) {
      const br = document.createElement("br");
      fragment.appendChild(br);
      lastNode = br;
    }

    if (line.length > 0) {
      const textNode = document.createTextNode(line);
      fragment.appendChild(textNode);
      lastNode = textNode;
    }
  });

  if (!lastNode) {
    lastNode = document.createTextNode("");
    fragment.appendChild(lastNode);
  }

  range.insertNode(fragment);
  range.setStartAfter(lastNode);
  range.collapse(true);
  selection.removeAllRanges();
  selection.addRange(range);
}

function insertHtml(
""".trimIndent()

        val matches = insertTextFunction.findAll(source).toList()
        if (matches.size != 1) {
            throw PatchException(
                "Could not safely identify exactly one Boxer insertText() function " +
                    "next to insertHtml(); found ${matches.size}. The Boxer version needs review."
            )
        }

        val patched = insertTextFunction.replaceFirst(source, replacement)
        selectionJs.writeText(patched)
    }
}
