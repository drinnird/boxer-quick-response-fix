package app.boxerpatches.quickresponses

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import java.security.MessageDigest

private const val EXPECTED_SELECTION_JS_SHA256 =
    "dab258022ee6490cddeceabb053586167ca81effd43984f91270f2ba9b55ba46"

private fun ByteArray.sha256Hex(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(this)
        .joinToString("") { "%02x".format(it) }

@Suppress("unused")
val fixQuickResponseBlankLinesApkMirror2606Patch = resourcePatch(
    name = "Fix Boxer Quick Response blank lines (APKMirror 26.06.0.215)",
    description = "Preserves blank lines in Quick Responses. Exact-match patch for Workspace ONE Boxer 26.06.0.215 (2877) from APKMirror.",
) {
    compatibleWith("com.boxer.email")

    execute {
        val selectionJs = get("assets/selection.js")
        val originalBytes = selectionJs.readBytes()
        val source = originalBytes.toString(Charsets.UTF_8)

        // Refuse to patch a different selection.js. This fingerprint is from the
        // APKMirror Boxer 26.06.0.215 (2877) base.apk supplied by the user.
        val actualHash = originalBytes.sha256Hex()
        if (actualHash != EXPECTED_SELECTION_JS_SHA256) {
            // Allow re-running Morphe on an already-patched copy without failing.
            if (source.contains("boxerQuickResponseNewlineFix")) return@execute

            throw PatchException(
                "Unexpected Boxer assets/selection.js. Expected SHA-256 " +
                    EXPECTED_SELECTION_JS_SHA256 + ", got " + actualHash +
                    ". No changes were made."
            )
        }

        val originalFunction = """
function insertText(text) {
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
}
""".trimIndent()

        val replacementFunction = """
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

  // Delete current selection.
  range.deleteContents();

  // emailstyle.css uses white-space: normal, so newline characters inside a
  // single text node collapse. Convert line breaks into actual BR nodes.
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

  // Move caret to end of inserted response.
  range.setStartAfter(lastNode);
  range.collapse(true);
  selection.removeAllRanges();
  selection.addRange(range);
}
""".trimIndent()

        val first = source.indexOf(originalFunction)
        val second = if (first >= 0) source.indexOf(originalFunction, first + 1) else -1
        if (first < 0 || second >= 0) {
            throw PatchException(
                "Exact Boxer insertText() block was not found exactly once. " +
                    "No changes were made."
            )
        }

        val patched = source.replaceFirst(originalFunction, replacementFunction)
        selectionJs.writeText(patched)
    }
}
