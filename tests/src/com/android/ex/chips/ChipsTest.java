/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ex.chips;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.util.Rfc822Tokenizer;
import android.widget.TextView;

import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;


public class ChipsTest extends AndroidTestCase {
    private RecipientChip[] mMockRecips;

    private RecipientEntry[] mMockEntries;

    private Rfc822Tokenizer mTokenizer;

    private Editable mEditable;

    class MockRecipientEditTextView extends RecipientEditTextView {

        public MockRecipientEditTextView(Context context) {
            super(context, null);
            mTokenizer = new Rfc822Tokenizer();
            setTokenizer(mTokenizer);
        }

        @Override
        public RecipientChip[] getSortedRecipients() {
            return mMockRecips;
        }

        @Override
        public Editable getText() {
            return mEditable;
        }

        @Override
        public Editable getSpannable() {
            return mEditable;
        }

        @Override
        public int getLineHeight() {
            return 48;
        }

        public Drawable getChipBackground(RecipientEntry contact) {
            return createChipBackground();
        }
    }

    private MockRecipientEditTextView createViewForTesting() {
        mEditable = new SpannableStringBuilder();
        MockRecipientEditTextView view = new MockRecipientEditTextView(getContext());
        return view;
    }

    public void testCreateDisplayText() {
        RecipientEditTextView view = createViewForTesting();
        RecipientEntry entry = RecipientEntry.constructGeneratedEntry("User Name, Jr",
                "user@username.com");
        String test = view.createDisplayText(entry);
        assertEquals("Expected a properly formatted RFC email address",
                "\"User Name, Jr\" <user@username.com>, ", test);

        RecipientEntry alreadyFormatted = RecipientEntry.constructFakeEntry("user@username.com, ");
        test = view.createDisplayText(alreadyFormatted);
        assertEquals("Expected a properly formatted RFC email address", "<user@username.com>, ",
                test);

        RecipientEntry alreadyFormattedNoSpace = RecipientEntry
                .constructFakeEntry("user@username.com,");
        test = view.createDisplayText(alreadyFormattedNoSpace);
        assertEquals("Expected a properly formatted RFC email address", "<user@username.com>, ",
                test);

        RecipientEntry alreadyNamed = RecipientEntry.constructGeneratedEntry("User Name",
                "\"User Name, Jr\" <user@username.com>");
        test = view.createDisplayText(alreadyNamed);
        assertEquals(
                "Expected address that used the name not the excess address name",
                "User Name <user@username.com>, ", test);
    }

    public void testSanitizeBetween() {
        populateMocks(2);
        MockRecipientEditTextView view = createViewForTesting();
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String extra = "EXTRA";
        mEditable = new SpannableStringBuilder();
        mEditable.append(first + extra + second);
        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], secondStart, secondEnd, 0);
        view.sanitizeBetween();
        String editableString = mEditable.toString();
        assertEquals(editableString.indexOf(extra), -1);
        assertEquals(editableString.indexOf(first), firstStart);
        assertEquals(editableString.indexOf(second), secondStart - extra.length());
        assertEquals(editableString, (first + second));

        mEditable = new SpannableStringBuilder();
        populateMocks(1);
        mEditable.append(extra);
        mEditable.append(first);
        firstStart = mEditable.toString().indexOf(first);
        firstEnd = firstStart + first.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], firstStart, firstEnd, 0);
        view.sanitizeBetween();
        assertEquals(mEditable.toString(), first);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), firstStart
                - extra.length());
    }

    public void testMoreChip() {
        populateMocks(3);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String third = (String) mTokenizer.terminateToken("THIRD");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first+second+third);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);

        view.createMoreChip();
        assertEquals(mEditable.toString(), first+second+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        // Find the more chip.
        ImageSpan moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), thirdStart);
        assertEquals(mEditable.getSpanEnd(moreChip), thirdEnd);

        view.removeMoreChip();
        assertEquals(mEditable.toString(), first+second+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), firstEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), thirdStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), thirdEnd);
        moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), -1);

        // Rinse and repeat, just in case!
        view.createMoreChip();
        assertEquals(mEditable.toString(), first+second+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        // Find the more chip.
        moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), thirdStart);
        assertEquals(mEditable.getSpanEnd(moreChip), thirdEnd);

        view.removeMoreChip();
        assertEquals(mEditable.toString(), first+second+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), firstEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), thirdStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), thirdEnd);
        moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), -1);
    }

    public void testMoreChipLotsOfUsers() {
        populateMocks(10);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String third = (String) mTokenizer.terminateToken("THIRD");
        String fourth = (String) mTokenizer.terminateToken("FOURTH");
        String fifth = (String) mTokenizer.terminateToken("FIFTH");
        String sixth = (String) mTokenizer.terminateToken("SIXTH");
        String seventh = (String) mTokenizer.terminateToken("SEVENTH");
        String eigth = (String) mTokenizer.terminateToken("EIGHTH");
        String ninth = (String) mTokenizer.terminateToken("NINTH");
        String tenth = (String) mTokenizer.terminateToken("TENTH");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first+second+third+fourth+fifth+sixth+seventh+eigth+ninth+tenth);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        int fourthStart = mEditable.toString().indexOf(fourth);
        int fourthEnd = fourthStart + fourth.length();
        int fifthStart = mEditable.toString().indexOf(fifth);
        int fifthEnd = fifthStart + fifth.length();
        int sixthStart = mEditable.toString().indexOf(sixth);
        int sixthEnd = sixthStart + sixth.length();
        int seventhStart = mEditable.toString().indexOf(seventh);
        int seventhEnd = seventhStart + seventh.length();
        int eighthStart = mEditable.toString().indexOf(eigth);
        int eighthEnd = eighthStart + eigth.length();
        int ninthStart = mEditable.toString().indexOf(ninth);
        int ninthEnd = ninthStart + ninth.length();
        int tenthStart = mEditable.toString().indexOf(tenth);
        int tenthEnd = tenthStart + tenth.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 10], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 9], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 8], thirdStart, thirdEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 7], fourthStart, fourthEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 6], fifthStart, fifthEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 5], sixthStart, sixthEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 4], seventhStart, seventhEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], eighthStart, eighthEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], ninthStart, ninthEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], tenthStart, tenthEnd, 0);

        view.createMoreChip();
        assertEquals(mEditable.toString(), first + second + third + fourth + fifth + sixth
                + seventh + eigth + ninth + tenth);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 10]), firstStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 9]), secondStart);
        // Find the more chip.
        ImageSpan moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), thirdStart);
        assertEquals(mEditable.getSpanEnd(moreChip), tenthEnd);

        view.removeMoreChip();
        assertEquals(mEditable.toString(), first + second + third + fourth + fifth + sixth
                + seventh + eigth + ninth + tenth);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 10]), firstStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 9]), secondStart);

        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 8]), thirdStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 7]), fourthStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 6]), fifthStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 5]), sixthStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 4]), seventhStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), eighthStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), ninthStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), tenthStart);
        moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), -1);

    }

    public void testMoreChipSpecialChars() {
        populateMocks(3);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        String first = (String) mTokenizer.terminateToken("FI,RST");
        String second = (String) mTokenizer.terminateToken("SE,COND");
        String third = (String) mTokenizer.terminateToken("THI,RD");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first+second+third);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);

        view.createMoreChip();
        assertEquals(mEditable.toString(), first+second+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        // Find the more chip.
        ImageSpan moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), thirdStart);
        assertEquals(mEditable.getSpanEnd(moreChip), thirdEnd);

        view.removeMoreChip();
        assertEquals(mEditable.toString(), first+second+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), firstEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), thirdStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), thirdEnd);
        moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), -1);
    }

    public void testMoreChipDupes() {
        populateMocks(4);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String third = (String) mTokenizer.terminateToken("THIRD");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first+second+third+third);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        int thirdNextStart = mEditable.toString().indexOf(third, thirdEnd);
        int thirdNextEnd = thirdNextStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 4], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], thirdStart, thirdEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdNextStart, thirdNextEnd, 0);

        view.createMoreChip();
        assertEquals(mEditable.toString(), first+second+third+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 4]), firstStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), secondStart);
        // Find the more chip.
        ImageSpan moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), thirdStart);
        assertEquals(mEditable.getSpanEnd(moreChip), thirdNextEnd);

        view.removeMoreChip();
        assertEquals(mEditable.toString(), first+second+third+third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 4]), firstStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 4]), firstEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), secondStart);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), thirdStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 2]), thirdEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), thirdNextStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), thirdNextEnd);
        moreChip = view.getMoreChip();
        assertEquals(mEditable.getSpanStart(moreChip), -1);
    }

    public void testMatchesChip() {
        populateMocks(3);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String third = (String) mTokenizer.terminateToken("THIRD");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first+second+third);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);
        assertFalse(view.matchesChip(mMockRecips[mMockRecips.length - 3], -1));
        assertFalse(view.matchesChip(mMockRecips[mMockRecips.length - 1], mEditable.length() + 1));
        assertTrue(view.matchesChip(mMockRecips[mMockRecips.length - 3], firstStart));
        assertTrue(view.matchesChip(mMockRecips[mMockRecips.length - 3], firstEnd));
        assertTrue(view.matchesChip(mMockRecips[mMockRecips.length - 3], firstEnd - 1));
    }

    public void testRemoveChip() {
        populateMocks(3);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String third = (String) mTokenizer.terminateToken("THIRD");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first + second + third);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);
        assertEquals(mEditable.toString(), first + second + third);
        view.removeChip(mMockRecips[mMockRecips.length - 2]);
        assertEquals(mEditable.toString(), first + third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), firstEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), -1);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 2]), -1);
        int newThirdStart = mEditable.toString().indexOf(third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), newThirdStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), newThirdStart
                + third.length());

        populateMocks(3);
        view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        mEditable = new SpannableStringBuilder();
        mEditable.append(first + second + third);

        firstStart = mEditable.toString().indexOf(first);
        firstEnd = firstStart + first.length();
        secondStart = mEditable.toString().indexOf(second);
        secondEnd = secondStart + second.length();
        thirdStart = mEditable.toString().indexOf(third);
        thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);
        assertEquals(mEditable.toString(), first + second + third);
        view.removeChip(mMockRecips[mMockRecips.length - 3]);
        assertEquals(mEditable.toString(), second + third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), -1);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), -1);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), 0);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 2]), second.length());
        newThirdStart = mEditable.toString().indexOf(third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), newThirdStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), newThirdStart
                + third.length());

        populateMocks(3);
        view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        mEditable = new SpannableStringBuilder();
        mEditable.append(first + second + third);

        firstStart = mEditable.toString().indexOf(first);
        firstEnd = firstStart + first.length();
        secondStart = mEditable.toString().indexOf(second);
        secondEnd = secondStart + second.length();
        thirdStart = mEditable.toString().indexOf(third);
        thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);
        assertEquals(mEditable.toString(), first + second + third);
        view.removeChip(mMockRecips[mMockRecips.length - 1]);
        assertEquals(mEditable.toString(), first + second);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), firstStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), firstEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), secondStart);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 2]), secondEnd);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), -1);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), -1);
    }

    public void testReplaceChip() {
        populateMocks(3);
        MockRecipientEditTextView view = createViewForTesting();
        view.setMoreItem(createTestMoreItem());
        view.setChipBackground(createChipBackground());
        view.setChipHeight(48);
        String first = (String) mTokenizer.terminateToken("FIRST");
        String second = (String) mTokenizer.terminateToken("SECOND");
        String third = (String) mTokenizer.terminateToken("THIRD");
        mEditable = new SpannableStringBuilder();
        mEditable.append(first + second + third);

        int firstStart = mEditable.toString().indexOf(first);
        int firstEnd = firstStart + first.length();
        int secondStart = mEditable.toString().indexOf(second);
        int secondEnd = secondStart + second.length();
        int thirdStart = mEditable.toString().indexOf(third);
        int thirdEnd = thirdStart + third.length();
        mEditable.setSpan(mMockRecips[mMockRecips.length - 3], firstStart, firstEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 2], secondStart, secondEnd, 0);
        mEditable.setSpan(mMockRecips[mMockRecips.length - 1], thirdStart, thirdEnd, 0);
        assertEquals(mEditable.toString(), first + second + third);
        view.replaceChip(mMockRecips[mMockRecips.length - 3], RecipientEntry
                .constructGeneratedEntry("replacement", "replacement@replacement.com"));
        assertEquals(mEditable.toString(), mTokenizer
                .terminateToken("replacement <replacement@replacement.com>")
                + second + third);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 3]), -1);
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 3]), -1);
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 2]), mEditable
                .toString().indexOf(second));
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 2]), mEditable
                .toString().indexOf(second)
                + second.length());
        assertEquals(mEditable.getSpanStart(mMockRecips[mMockRecips.length - 1]), mEditable
                .toString().indexOf(third));
        assertEquals(mEditable.getSpanEnd(mMockRecips[mMockRecips.length - 1]), mEditable
                .toString().indexOf(third)
                + third.length());
        RecipientChip[] spans = mEditable.getSpans(0, mEditable.length(), RecipientChip.class);
        assertEquals(spans.length, 3);
        spans = mEditable
                .getSpans(0, mEditable.toString().indexOf(second) - 1, RecipientChip.class);
        assertEquals((String) spans[0].getDisplay(), "replacement");
    }

    private Drawable createChipBackground() {
        Bitmap drawable = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        return new BitmapDrawable(getContext().getResources(), drawable);
    }

    private TextView createTestMoreItem() {
        TextView view = new TextView(getContext());
        view.setText("<xliff:g id='count'>%1$s</xliff:g> more...");
        return view;
    }

    private void populateMocks(int size) {
        mMockEntries = new RecipientEntry[size];
        for (int i = 0; i < size; i++) {
            mMockEntries[i] = RecipientEntry.constructGeneratedEntry("user",
                    "user@username.com");
        }
        mMockRecips = new RecipientChip[size];
        for (int i = 0; i < size; i++) {
            mMockRecips[i] = new RecipientChip(null, mMockEntries[i], i);
        }
    }
}
