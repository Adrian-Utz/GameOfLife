# Fixed

## Conway.java
- Initialized the no-arg constructor, added validation for empty uneven or invalid pattern rows, closed the file scanner, replaced the nonstandard `main()` with Java's standard `main(String[] args)`, and moved simulation updates to a Swing timer so the UI stays responsive.

## GrinCanvas.java
- Rejects invalid dimensions, wraps negative coordinates reliably, and preserves the cell grid durring serialization.

## Cell.java
- Made cels serializable so a restored grid retains its state.