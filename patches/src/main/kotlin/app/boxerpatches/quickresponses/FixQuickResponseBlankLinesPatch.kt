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

        val original = """
  // Delete current selection
  range.deleteContents();

  // Insert the text
  const textNode = document.createTextNode(text);
  range.insertNode(textNode);

  // Move caret to end of inserted text
  range.setStartAfter(textNode);
  range.collapse(true);
  selection.removeAllRanges();
  selection.addRange(range);
""".trimIndent()

        val replacement = """
  // Delete current selection.
  range.deleteContents();

  // Boxer renders the composer with white-space: normal, so newline
  // characters inside one text node collapse. Convert newlines to real
  // <br> nodes so blank lines in Quick Responses are preserved.
  const normalizedText = String(text).replace(/\\r\\n?/g, "\\n");
  const lines = normalizedText.split("\\n");
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

  // Move caret to end of inserted content.
  range.setStartAfter(lastNode);
  range.collapse(true);
  selection.removeAllRanges();
  selection.addRange(range);
""".trimIndent()

        if (!source.contains(original)) {
            if (source.contains("const normalizedText = String(text).replace")) {
                // Already patched; leave it alone.
                return@execute
            }
            throw PatchException(
                "Boxer assets/selection.js did not contain the expected insertText block. " +
                    "This Boxer version may need an updated patch."
            )
        }

        selectionJs.writeText(source.replace(original, replacement))
    }
}
