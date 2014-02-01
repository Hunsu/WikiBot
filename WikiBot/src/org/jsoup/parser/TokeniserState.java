package org.jsoup.parser;

/**
 * States and transition activations for the Tokeniser.
 */
enum TokeniserState {
    
    /** The Data. */
    Data {
        // in data state, gather characters until a character reference or tag is found
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '&':
                    t.advanceTransition(CharacterReferenceInData);
                    break;
                case '<':
                    t.advanceTransition(TagOpen);
                    break;
                case nullChar:
                    t.error(this); // NOT replacement character (oddly?)
                    t.emit(r.consume());
                    break;
                case eof:
                    t.emit(new Token.EOF());
                    break;
                default:
                    String data = r.consumeToAny('&', '<', nullChar);
                    t.emit(data);
                    break;
            }
        }
    },
    
    /** The Character reference in data. */
    CharacterReferenceInData {
        // from & in data
        void read(Tokeniser t, CharacterReader r) {
            char[] c = t.consumeCharacterReference(null, false);
            if (c == null)
                t.emit('&');
            else
                t.emit(c);
            t.transition(Data);
        }
    },
    
    /** The Rcdata. */
    Rcdata {
        /// handles data in title, textarea etc
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '&':
                    t.advanceTransition(CharacterReferenceInRcdata);
                    break;
                case '<':
                    t.advanceTransition(RcdataLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.emit(replacementChar);
                    break;
                case eof:
                    t.emit(new Token.EOF());
                    break;
                default:
                    String data = r.consumeToAny('&', '<', nullChar);
                    t.emit(data);
                    break;
            }
        }
    },
    
    /** The Character reference in rcdata. */
    CharacterReferenceInRcdata {
        void read(Tokeniser t, CharacterReader r) {
            char[] c = t.consumeCharacterReference(null, false);
            if (c == null)
                t.emit('&');
            else
                t.emit(c);
            t.transition(Rcdata);
        }
    },
    
    /** The Rawtext. */
    Rawtext {
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '<':
                    t.advanceTransition(RawtextLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.emit(replacementChar);
                    break;
                case eof:
                    t.emit(new Token.EOF());
                    break;
                default:
                    String data = r.consumeToAny('<', nullChar);
                    t.emit(data);
                    break;
            }
        }
    },
    
    /** The Script data. */
    ScriptData {
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '<':
                    t.advanceTransition(ScriptDataLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.emit(replacementChar);
                    break;
                case eof:
                    t.emit(new Token.EOF());
                    break;
                default:
                    String data = r.consumeToAny('<', nullChar);
                    t.emit(data);
                    break;
            }
        }
    },
    
    /** The plaintext. */
    PLAINTEXT {
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.emit(replacementChar);
                    break;
                case eof:
                    t.emit(new Token.EOF());
                    break;
                default:
                    String data = r.consumeTo(nullChar);
                    t.emit(data);
                    break;
            }
        }
    },
    
    /** The Tag open. */
    TagOpen {
        // from < in data
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '!':
                    t.advanceTransition(MarkupDeclarationOpen);
                    break;
                case '/':
                    t.advanceTransition(EndTagOpen);
                    break;
                case '?':
                    t.advanceTransition(BogusComment);
                    break;
                default:
                    if (r.matchesLetter()) {
                        t.createTagPending(true);
                        t.transition(TagName);
                    } else {
                        t.error(this);
                        t.emit('<'); // char that got us here
                        t.transition(Data);
                    }
                    break;
            }
        }
    },
    
    /** The End tag open. */
    EndTagOpen {
        void read(Tokeniser t, CharacterReader r) {
            if (r.isEmpty()) {
                t.eofError(this);
                t.emit("</");
                t.transition(Data);
            } else if (r.matchesLetter()) {
                t.createTagPending(false);
                t.transition(TagName);
            } else if (r.matches('>')) {
                t.error(this);
                t.advanceTransition(Data);
            } else {
                t.error(this);
                t.advanceTransition(BogusComment);
            }
        }
    },
    
    /** The Tag name. */
    TagName {
        // from < or </ in data, will have start or end tag pending
        void read(Tokeniser t, CharacterReader r) {
            // previous TagOpen state did NOT consume, will have a letter char in current
            String tagName = r.consumeToAny('\t', '\n', '\r', '\f', ' ', '/', '>', nullChar).toLowerCase();
            t.tagPending.appendTagName(tagName);

            switch (r.consume()) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeAttributeName);
                    break;
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar: // replacement
                    t.tagPending.appendTagName(replacementStr);
                    break;
                case eof: // should emit pending tag?
                    t.eofError(this);
                    t.transition(Data);
                // no default, as covered with above consumeToAny
            }
        }
    },
    
    /** The Rcdata lessthan sign. */
    RcdataLessthanSign {
        // from < in rcdata
        void read(Tokeniser t, CharacterReader r) {
            if (r.matches('/')) {
                t.createTempBuffer();
                t.advanceTransition(RCDATAEndTagOpen);
            } else if (r.matchesLetter() && !r.containsIgnoreCase("</" + t.appropriateEndTagName())) {
                // diverge from spec: got a start tag, but there's no appropriate end tag (</title>), so rather than
                // consuming to EOF; break out here
                t.tagPending = new Token.EndTag(t.appropriateEndTagName());
                t.emitTagPending();
                r.unconsume(); // undo "<"
                t.transition(Data);
            } else {
                t.emit("<");
                t.transition(Rcdata);
            }
        }
    },
    
    /** The RCDATA end tag open. */
    RCDATAEndTagOpen {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                t.createTagPending(false);
                t.tagPending.appendTagName(Character.toLowerCase(r.current()));
                t.dataBuffer.append(Character.toLowerCase(r.current()));
                t.advanceTransition(RCDATAEndTagName);
            } else {
                t.emit("</");
                t.transition(Rcdata);
            }
        }
    },
    
    /** The RCDATA end tag name. */
    RCDATAEndTagName {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                String name = r.consumeLetterSequence();
                t.tagPending.appendTagName(name.toLowerCase());
                t.dataBuffer.append(name);
                return;
            }

            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    if (t.isAppropriateEndTagToken())
                        t.transition(BeforeAttributeName);
                    else
                        anythingElse(t, r);
                    break;
                case '/':
                    if (t.isAppropriateEndTagToken())
                        t.transition(SelfClosingStartTag);
                    else
                        anythingElse(t, r);
                    break;
                case '>':
                    if (t.isAppropriateEndTagToken()) {
                        t.emitTagPending();
                        t.transition(Data);
                    }
                    else
                        anythingElse(t, r);
                    break;
                default:
                    anythingElse(t, r);
            }
        }

        private void anythingElse(Tokeniser t, CharacterReader r) {
            t.emit("</" + t.dataBuffer.toString());
            r.unconsume();
            t.transition(Rcdata);
        }
    },
    
    /** The Rawtext lessthan sign. */
    RawtextLessthanSign {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matches('/')) {
                t.createTempBuffer();
                t.advanceTransition(RawtextEndTagOpen);
            } else {
                t.emit('<');
                t.transition(Rawtext);
            }
        }
    },
    
    /** The Rawtext end tag open. */
    RawtextEndTagOpen {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                t.createTagPending(false);
                t.transition(RawtextEndTagName);
            } else {
                t.emit("</");
                t.transition(Rawtext);
            }
        }
    },
    
    /** The Rawtext end tag name. */
    RawtextEndTagName {
        void read(Tokeniser t, CharacterReader r) {
            handleDataEndTag(t, r, Rawtext);
        }
    },
    
    /** The Script data lessthan sign. */
    ScriptDataLessthanSign {
        void read(Tokeniser t, CharacterReader r) {
            switch (r.consume()) {
                case '/':
                    t.createTempBuffer();
                    t.transition(ScriptDataEndTagOpen);
                    break;
                case '!':
                    t.emit("<!");
                    t.transition(ScriptDataEscapeStart);
                    break;
                default:
                    t.emit("<");
                    r.unconsume();
                    t.transition(ScriptData);
            }
        }
    },
    
    /** The Script data end tag open. */
    ScriptDataEndTagOpen {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                t.createTagPending(false);
                t.transition(ScriptDataEndTagName);
            } else {
                t.emit("</");
                t.transition(ScriptData);
            }

        }
    },
    
    /** The Script data end tag name. */
    ScriptDataEndTagName {
        void read(Tokeniser t, CharacterReader r) {
            handleDataEndTag(t, r, ScriptData);
        }
    },
    
    /** The Script data escape start. */
    ScriptDataEscapeStart {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matches('-')) {
                t.emit('-');
                t.advanceTransition(ScriptDataEscapeStartDash);
            } else {
                t.transition(ScriptData);
            }
        }
    },
    
    /** The Script data escape start dash. */
    ScriptDataEscapeStartDash {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matches('-')) {
                t.emit('-');
                t.advanceTransition(ScriptDataEscapedDashDash);
            } else {
                t.transition(ScriptData);
            }
        }
    },
    
    /** The Script data escaped. */
    ScriptDataEscaped {
        void read(Tokeniser t, CharacterReader r) {
            if (r.isEmpty()) {
                t.eofError(this);
                t.transition(Data);
                return;
            }

            switch (r.current()) {
                case '-':
                    t.emit('-');
                    t.advanceTransition(ScriptDataEscapedDash);
                    break;
                case '<':
                    t.advanceTransition(ScriptDataEscapedLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.emit(replacementChar);
                    break;
                default:
                    String data = r.consumeToAny('-', '<', nullChar);
                    t.emit(data);
            }
        }
    },
    
    /** The Script data escaped dash. */
    ScriptDataEscapedDash {
        void read(Tokeniser t, CharacterReader r) {
            if (r.isEmpty()) {
                t.eofError(this);
                t.transition(Data);
                return;
            }

            char c = r.consume();
            switch (c) {
                case '-':
                    t.emit(c);
                    t.transition(ScriptDataEscapedDashDash);
                    break;
                case '<':
                    t.transition(ScriptDataEscapedLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    t.emit(replacementChar);
                    t.transition(ScriptDataEscaped);
                    break;
                default:
                    t.emit(c);
                    t.transition(ScriptDataEscaped);
            }
        }
    },
    
    /** The Script data escaped dash dash. */
    ScriptDataEscapedDashDash {
        void read(Tokeniser t, CharacterReader r) {
            if (r.isEmpty()) {
                t.eofError(this);
                t.transition(Data);
                return;
            }

            char c = r.consume();
            switch (c) {
                case '-':
                    t.emit(c);
                    break;
                case '<':
                    t.transition(ScriptDataEscapedLessthanSign);
                    break;
                case '>':
                    t.emit(c);
                    t.transition(ScriptData);
                    break;
                case nullChar:
                    t.error(this);
                    t.emit(replacementChar);
                    t.transition(ScriptDataEscaped);
                    break;
                default:
                    t.emit(c);
                    t.transition(ScriptDataEscaped);
            }
        }
    },
    
    /** The Script data escaped lessthan sign. */
    ScriptDataEscapedLessthanSign {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                t.createTempBuffer();
                t.dataBuffer.append(Character.toLowerCase(r.current()));
                t.emit("<" + r.current());
                t.advanceTransition(ScriptDataDoubleEscapeStart);
            } else if (r.matches('/')) {
                t.createTempBuffer();
                t.advanceTransition(ScriptDataEscapedEndTagOpen);
            } else {
                t.emit('<');
                t.transition(ScriptDataEscaped);
            }
        }
    },
    
    /** The Script data escaped end tag open. */
    ScriptDataEscapedEndTagOpen {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                t.createTagPending(false);
                t.tagPending.appendTagName(Character.toLowerCase(r.current()));
                t.dataBuffer.append(r.current());
                t.advanceTransition(ScriptDataEscapedEndTagName);
            } else {
                t.emit("</");
                t.transition(ScriptDataEscaped);
            }
        }
    },
    
    /** The Script data escaped end tag name. */
    ScriptDataEscapedEndTagName {
        void read(Tokeniser t, CharacterReader r) {
            handleDataEndTag(t, r, ScriptDataEscaped);
        }
    },
    
    /** The Script data double escape start. */
    ScriptDataDoubleEscapeStart {
        void read(Tokeniser t, CharacterReader r) {
            handleDataDoubleEscapeTag(t, r, ScriptDataDoubleEscaped, ScriptDataEscaped);
        }
    },
    
    /** The Script data double escaped. */
    ScriptDataDoubleEscaped {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.current();
            switch (c) {
                case '-':
                    t.emit(c);
                    t.advanceTransition(ScriptDataDoubleEscapedDash);
                    break;
                case '<':
                    t.emit(c);
                    t.advanceTransition(ScriptDataDoubleEscapedLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.emit(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default:
                    String data = r.consumeToAny('-', '<', nullChar);
                    t.emit(data);
            }
        }
    },
    
    /** The Script data double escaped dash. */
    ScriptDataDoubleEscapedDash {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '-':
                    t.emit(c);
                    t.transition(ScriptDataDoubleEscapedDashDash);
                    break;
                case '<':
                    t.emit(c);
                    t.transition(ScriptDataDoubleEscapedLessthanSign);
                    break;
                case nullChar:
                    t.error(this);
                    t.emit(replacementChar);
                    t.transition(ScriptDataDoubleEscaped);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default:
                    t.emit(c);
                    t.transition(ScriptDataDoubleEscaped);
            }
        }
    },
    
    /** The Script data double escaped dash dash. */
    ScriptDataDoubleEscapedDashDash {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '-':
                    t.emit(c);
                    break;
                case '<':
                    t.emit(c);
                    t.transition(ScriptDataDoubleEscapedLessthanSign);
                    break;
                case '>':
                    t.emit(c);
                    t.transition(ScriptData);
                    break;
                case nullChar:
                    t.error(this);
                    t.emit(replacementChar);
                    t.transition(ScriptDataDoubleEscaped);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default:
                    t.emit(c);
                    t.transition(ScriptDataDoubleEscaped);
            }
        }
    },
    
    /** The Script data double escaped lessthan sign. */
    ScriptDataDoubleEscapedLessthanSign {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matches('/')) {
                t.emit('/');
                t.createTempBuffer();
                t.advanceTransition(ScriptDataDoubleEscapeEnd);
            } else {
                t.transition(ScriptDataDoubleEscaped);
            }
        }
    },
    
    /** The Script data double escape end. */
    ScriptDataDoubleEscapeEnd {
        void read(Tokeniser t, CharacterReader r) {
            handleDataDoubleEscapeTag(t,r, ScriptDataEscaped, ScriptDataDoubleEscaped);
        }
    },
    
    /** The Before attribute name. */
    BeforeAttributeName {
        // from tagname <xxx
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break; // ignore whitespace
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.newAttribute();
                    r.unconsume();
                    t.transition(AttributeName);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                case '"':
                case '\'':
                case '<':
                case '=':
                    t.error(this);
                    t.tagPending.newAttribute();
                    t.tagPending.appendAttributeName(c);
                    t.transition(AttributeName);
                    break;
                default: // A-Z, anything else
                    t.tagPending.newAttribute();
                    r.unconsume();
                    t.transition(AttributeName);
            }
        }
    },
    
    /** The Attribute name. */
    AttributeName {
        // from before attribute name
        void read(Tokeniser t, CharacterReader r) {
            String name = r.consumeToAny('\t', '\n', '\r', '\f', ' ', '/', '=', '>', nullChar, '"', '\'', '<');
            t.tagPending.appendAttributeName(name.toLowerCase());

            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(AfterAttributeName);
                    break;
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                case '=':
                    t.transition(BeforeAttributeValue);
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.appendAttributeName(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                case '"':
                case '\'':
                case '<':
                    t.error(this);
                    t.tagPending.appendAttributeName(c);
                // no default, as covered in consumeToAny
            }
        }
    },
    
    /** The After attribute name. */
    AfterAttributeName {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    // ignore
                    break;
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                case '=':
                    t.transition(BeforeAttributeValue);
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.appendAttributeName(replacementChar);
                    t.transition(AttributeName);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                case '"':
                case '\'':
                case '<':
                    t.error(this);
                    t.tagPending.newAttribute();
                    t.tagPending.appendAttributeName(c);
                    t.transition(AttributeName);
                    break;
                default: // A-Z, anything else
                    t.tagPending.newAttribute();
                    r.unconsume();
                    t.transition(AttributeName);
            }
        }
    },
    
    /** The Before attribute value. */
    BeforeAttributeValue {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    // ignore
                    break;
                case '"':
                    t.transition(AttributeValue_doubleQuoted);
                    break;
                case '&':
                    r.unconsume();
                    t.transition(AttributeValue_unquoted);
                    break;
                case '\'':
                    t.transition(AttributeValue_singleQuoted);
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.appendAttributeValue(replacementChar);
                    t.transition(AttributeValue_unquoted);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                case '>':
                    t.error(this);
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case '<':
                case '=':
                case '`':
                    t.error(this);
                    t.tagPending.appendAttributeValue(c);
                    t.transition(AttributeValue_unquoted);
                    break;
                default:
                    r.unconsume();
                    t.transition(AttributeValue_unquoted);
            }
        }
    },
    
    /** The Attribute value_double quoted. */
    AttributeValue_doubleQuoted {
        void read(Tokeniser t, CharacterReader r) {
            String value = r.consumeToAny('"', '&', nullChar);
            if (value.length() > 0)
                t.tagPending.appendAttributeValue(value);

            char c = r.consume();
            switch (c) {
                case '"':
                    t.transition(AfterAttributeValue_quoted);
                    break;
                case '&':
                    char[] ref = t.consumeCharacterReference('"', true);
                    if (ref != null)
                        t.tagPending.appendAttributeValue(ref);
                    else
                        t.tagPending.appendAttributeValue('&');
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.appendAttributeValue(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                // no default, handled in consume to any above
            }
        }
    },
    
    /** The Attribute value_single quoted. */
    AttributeValue_singleQuoted {
        void read(Tokeniser t, CharacterReader r) {
            String value = r.consumeToAny('\'', '&', nullChar);
            if (value.length() > 0)
                t.tagPending.appendAttributeValue(value);

            char c = r.consume();
            switch (c) {
                case '\'':
                    t.transition(AfterAttributeValue_quoted);
                    break;
                case '&':
                    char[] ref = t.consumeCharacterReference('\'', true);
                    if (ref != null)
                        t.tagPending.appendAttributeValue(ref);
                    else
                        t.tagPending.appendAttributeValue('&');
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.appendAttributeValue(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                // no default, handled in consume to any above
            }
        }
    },
    
    /** The Attribute value_unquoted. */
    AttributeValue_unquoted {
        void read(Tokeniser t, CharacterReader r) {
            String value = r.consumeToAny('\t', '\n', '\r', '\f', ' ', '&', '>', nullChar, '"', '\'', '<', '=', '`');
            if (value.length() > 0)
                t.tagPending.appendAttributeValue(value);

            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeAttributeName);
                    break;
                case '&':
                    char[] ref = t.consumeCharacterReference('>', true);
                    if (ref != null)
                        t.tagPending.appendAttributeValue(ref);
                    else
                        t.tagPending.appendAttributeValue('&');
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.appendAttributeValue(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                case '"':
                case '\'':
                case '<':
                case '=':
                case '`':
                    t.error(this);
                    t.tagPending.appendAttributeValue(c);
                    break;
                // no default, handled in consume to any above
            }

        }
    },
    // CharacterReferenceInAttributeValue state handled inline
    /** The After attribute value_quoted. */
    AfterAttributeValue_quoted {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeAttributeName);
                    break;
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    r.unconsume();
                    t.transition(BeforeAttributeName);
            }

        }
    },
    
    /** The Self closing start tag. */
    SelfClosingStartTag {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '>':
                    t.tagPending.selfClosing = true;
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.transition(BeforeAttributeName);
            }
        }
    },
    
    /** The Bogus comment. */
    BogusComment {
        void read(Tokeniser t, CharacterReader r) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            // rewind to capture character that lead us here
            r.unconsume();
            Token.Comment comment = new Token.Comment();
            comment.bogus = true;
            comment.data.append(r.consumeTo('>'));
            // todo: replace nullChar with replaceChar
            t.emit(comment);
            t.advanceTransition(Data);
        }
    },
    
    /** The Markup declaration open. */
    MarkupDeclarationOpen {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchConsume("--")) {
                t.createCommentPending();
                t.transition(CommentStart);
            } else if (r.matchConsumeIgnoreCase("DOCTYPE")) {
                t.transition(Doctype);
            } else if (r.matchConsume("[CDATA[")) {
                // todo: should actually check current namepspace, and only non-html allows cdata. until namespace
                // is implemented properly, keep handling as cdata
                //} else if (!t.currentNodeInHtmlNS() && r.matchConsume("[CDATA[")) {
                t.transition(CdataSection);
            } else {
                t.error(this);
                t.advanceTransition(BogusComment); // advance so this character gets in bogus comment data's rewind
            }
        }
    },
    
    /** The Comment start. */
    CommentStart {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '-':
                    t.transition(CommentStartDash);
                    break;
                case nullChar:
                    t.error(this);
                    t.commentPending.data.append(replacementChar);
                    t.transition(Comment);
                    break;
                case '>':
                    t.error(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                default:
                    t.commentPending.data.append(c);
                    t.transition(Comment);
            }
        }
    },
    
    /** The Comment start dash. */
    CommentStartDash {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '-':
                    t.transition(CommentStartDash);
                    break;
                case nullChar:
                    t.error(this);
                    t.commentPending.data.append(replacementChar);
                    t.transition(Comment);
                    break;
                case '>':
                    t.error(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                default:
                    t.commentPending.data.append(c);
                    t.transition(Comment);
            }
        }
    },
    
    /** The Comment. */
    Comment {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.current();
            switch (c) {
                case '-':
                    t.advanceTransition(CommentEndDash);
                    break;
                case nullChar:
                    t.error(this);
                    r.advance();
                    t.commentPending.data.append(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                default:
                    t.commentPending.data.append(r.consumeToAny('-', nullChar));
            }
        }
    },
    
    /** The Comment end dash. */
    CommentEndDash {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '-':
                    t.transition(CommentEnd);
                    break;
                case nullChar:
                    t.error(this);
                    t.commentPending.data.append('-').append(replacementChar);
                    t.transition(Comment);
                    break;
                case eof:
                    t.eofError(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                default:
                    t.commentPending.data.append('-').append(c);
                    t.transition(Comment);
            }
        }
    },
    
    /** The Comment end. */
    CommentEnd {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '>':
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.commentPending.data.append("--").append(replacementChar);
                    t.transition(Comment);
                    break;
                case '!':
                    t.error(this);
                    t.transition(CommentEndBang);
                    break;
                case '-':
                    t.error(this);
                    t.commentPending.data.append('-');
                    break;
                case eof:
                    t.eofError(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.commentPending.data.append("--").append(c);
                    t.transition(Comment);
            }
        }
    },
    
    /** The Comment end bang. */
    CommentEndBang {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '-':
                    t.commentPending.data.append("--!");
                    t.transition(CommentEndDash);
                    break;
                case '>':
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.commentPending.data.append("--!").append(replacementChar);
                    t.transition(Comment);
                    break;
                case eof:
                    t.eofError(this);
                    t.emitCommentPending();
                    t.transition(Data);
                    break;
                default:
                    t.commentPending.data.append("--!").append(c);
                    t.transition(Comment);
            }
        }
    },
    
    /** The Doctype. */
    Doctype {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeDoctypeName);
                    break;
                case eof:
                    t.eofError(this);
                    t.createDoctypePending();
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.transition(BeforeDoctypeName);
            }
        }
    },
    
    /** The Before doctype name. */
    BeforeDoctypeName {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                t.createDoctypePending();
                t.transition(DoctypeName);
                return;
            }
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break; // ignore whitespace
                case nullChar:
                    t.error(this);
                    t.doctypePending.name.append(replacementChar);
                    t.transition(DoctypeName);
                    break;
                case eof:
                    t.eofError(this);
                    t.createDoctypePending();
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.createDoctypePending();
                    t.doctypePending.name.append(c);
                    t.transition(DoctypeName);
            }
        }
    },
    
    /** The Doctype name. */
    DoctypeName {
        void read(Tokeniser t, CharacterReader r) {
            if (r.matchesLetter()) {
                String name = r.consumeLetterSequence();
                t.doctypePending.name.append(name.toLowerCase());
                return;
            }
            char c = r.consume();
            switch (c) {
                case '>':
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(AfterDoctypeName);
                    break;
                case nullChar:
                    t.error(this);
                    t.doctypePending.name.append(replacementChar);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.doctypePending.name.append(c);
            }
        }
    },
    
    /** The After doctype name. */
    AfterDoctypeName {
        void read(Tokeniser t, CharacterReader r) {
            if (r.isEmpty()) {
                t.eofError(this);
                t.doctypePending.forceQuirks = true;
                t.emitDoctypePending();
                t.transition(Data);
                return;
            }
            if (r.matchesAny('\t', '\n', '\r', '\f', ' '))
                r.advance(); // ignore whitespace
            else if (r.matches('>')) {
                t.emitDoctypePending();
                t.advanceTransition(Data);
            } else if (r.matchConsumeIgnoreCase("PUBLIC")) {
                t.transition(AfterDoctypePublicKeyword);
            } else if (r.matchConsumeIgnoreCase("SYSTEM")) {
                t.transition(AfterDoctypeSystemKeyword);
            } else {
                t.error(this);
                t.doctypePending.forceQuirks = true;
                t.advanceTransition(BogusDoctype);
            }

        }
    },
    
    /** The After doctype public keyword. */
    AfterDoctypePublicKeyword {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeDoctypePublicIdentifier);
                    break;
                case '"':
                    t.error(this);
                    // set public id to empty string
                    t.transition(DoctypePublicIdentifier_doubleQuoted);
                    break;
                case '\'':
                    t.error(this);
                    // set public id to empty string
                    t.transition(DoctypePublicIdentifier_singleQuoted);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.transition(BogusDoctype);
            }
        }
    },
    
    /** The Before doctype public identifier. */
    BeforeDoctypePublicIdentifier {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break;
                case '"':
                    // set public id to empty string
                    t.transition(DoctypePublicIdentifier_doubleQuoted);
                    break;
                case '\'':
                    // set public id to empty string
                    t.transition(DoctypePublicIdentifier_singleQuoted);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.transition(BogusDoctype);
            }
        }
    },
    
    /** The Doctype public identifier_double quoted. */
    DoctypePublicIdentifier_doubleQuoted {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '"':
                    t.transition(AfterDoctypePublicIdentifier);
                    break;
                case nullChar:
                    t.error(this);
                    t.doctypePending.publicIdentifier.append(replacementChar);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.doctypePending.publicIdentifier.append(c);
            }
        }
    },
    
    /** The Doctype public identifier_single quoted. */
    DoctypePublicIdentifier_singleQuoted {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\'':
                    t.transition(AfterDoctypePublicIdentifier);
                    break;
                case nullChar:
                    t.error(this);
                    t.doctypePending.publicIdentifier.append(replacementChar);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.doctypePending.publicIdentifier.append(c);
            }
        }
    },
    
    /** The After doctype public identifier. */
    AfterDoctypePublicIdentifier {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BetweenDoctypePublicAndSystemIdentifiers);
                    break;
                case '>':
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case '"':
                    t.error(this);
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted);
                    break;
                case '\'':
                    t.error(this);
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.transition(BogusDoctype);
            }
        }
    },
    
    /** The Between doctype public and system identifiers. */
    BetweenDoctypePublicAndSystemIdentifiers {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break;
                case '>':
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case '"':
                    t.error(this);
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted);
                    break;
                case '\'':
                    t.error(this);
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.transition(BogusDoctype);
            }
        }
    },
    
    /** The After doctype system keyword. */
    AfterDoctypeSystemKeyword {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeDoctypeSystemIdentifier);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case '"':
                    t.error(this);
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted);
                    break;
                case '\'':
                    t.error(this);
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
            }
        }
    },
    
    /** The Before doctype system identifier. */
    BeforeDoctypeSystemIdentifier {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break;
                case '"':
                    // set system id to empty string
                    t.transition(DoctypeSystemIdentifier_doubleQuoted);
                    break;
                case '\'':
                    // set public id to empty string
                    t.transition(DoctypeSystemIdentifier_singleQuoted);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.transition(BogusDoctype);
            }
        }
    },
    
    /** The Doctype system identifier_double quoted. */
    DoctypeSystemIdentifier_doubleQuoted {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '"':
                    t.transition(AfterDoctypeSystemIdentifier);
                    break;
                case nullChar:
                    t.error(this);
                    t.doctypePending.systemIdentifier.append(replacementChar);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.doctypePending.systemIdentifier.append(c);
            }
        }
    },
    
    /** The Doctype system identifier_single quoted. */
    DoctypeSystemIdentifier_singleQuoted {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\'':
                    t.transition(AfterDoctypeSystemIdentifier);
                    break;
                case nullChar:
                    t.error(this);
                    t.doctypePending.systemIdentifier.append(replacementChar);
                    break;
                case '>':
                    t.error(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.doctypePending.systemIdentifier.append(c);
            }
        }
    },
    
    /** The After doctype system identifier. */
    AfterDoctypeSystemIdentifier {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break;
                case '>':
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.eofError(this);
                    t.doctypePending.forceQuirks = true;
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    t.error(this);
                    t.transition(BogusDoctype);
                    // NOT force quirks
            }
        }
    },
    
    /** The Bogus doctype. */
    BogusDoctype {
        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '>':
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                case eof:
                    t.emitDoctypePending();
                    t.transition(Data);
                    break;
                default:
                    // ignore char
                    break;
            }
        }
    },
    
    /** The Cdata section. */
    CdataSection {
        void read(Tokeniser t, CharacterReader r) {
            String data = r.consumeTo("]]>");
            t.emit(data);
            r.matchConsume("]]>");
            t.transition(Data);
        }
    };


    /**
     * Read.
     *
     * @param t the t
     * @param r the r
     */
    abstract void read(Tokeniser t, CharacterReader r);

    /** The Constant nullChar. */
    private static final char nullChar = '\u0000';
    
    /** The Constant replacementChar. */
    private static final char replacementChar = Tokeniser.replacementChar;
    
    /** The Constant replacementStr. */
    private static final String replacementStr = String.valueOf(Tokeniser.replacementChar);
    
    /** The Constant eof. */
    private static final char eof = CharacterReader.EOF;

    /**
     * Handles RawtextEndTagName, ScriptDataEndTagName, and ScriptDataEscapedEndTagName. Same body impl, just
     * different else exit transitions.
     *
     * @param t the t
     * @param r the r
     * @param elseTransition the else transition
     */
    private static final void handleDataEndTag(Tokeniser t, CharacterReader r, TokeniserState elseTransition) {
        if (r.matchesLetter()) {
            String name = r.consumeLetterSequence();
            t.tagPending.appendTagName(name.toLowerCase());
            t.dataBuffer.append(name);
            return;
        }

        boolean needsExitTransition = false;
        if (t.isAppropriateEndTagToken() && !r.isEmpty()) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeAttributeName);
                    break;
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                default:
                    t.dataBuffer.append(c);
                    needsExitTransition = true;
            }
        } else {
            needsExitTransition = true;
        }

        if (needsExitTransition) {
            t.emit("</" + t.dataBuffer.toString());
            t.transition(elseTransition);
        }
    }

    /**
     * Handle data double escape tag.
     *
     * @param t the t
     * @param r the r
     * @param primary the primary
     * @param fallback the fallback
     */
    private static final void handleDataDoubleEscapeTag(Tokeniser t, CharacterReader r, TokeniserState primary, TokeniserState fallback) {
        if (r.matchesLetter()) {
            String name = r.consumeLetterSequence();
            t.dataBuffer.append(name.toLowerCase());
            t.emit(name);
            return;
        }

        char c = r.consume();
        switch (c) {
            case '\t':
            case '\n':
            case '\r':
            case '\f':
            case ' ':
            case '/':
            case '>':
                if (t.dataBuffer.toString().equals("script"))
                    t.transition(primary);
                else
                    t.transition(fallback);
                t.emit(c);
                break;
            default:
                r.unconsume();
                t.transition(fallback);
        }
    }
}