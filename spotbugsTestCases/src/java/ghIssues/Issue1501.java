package ghIssues;

import javax.annotation.Nonnull;

public class Issue1501 {
    void testNewlineFormatVariable(){
        String greeting = "hello world";
        String ending = " ending!!!";
        String stupid = "not used";
        String.format(greeting + " Fantastic.\n" + ending + " greatness.\n"); // no warning (false negative)
        String nonsense = "goodness ";
        String.format(stupid + nonsense); // no warning (false negative)
        String.format("great\n");
    }

    void testNewlineFormatString() {
        String.format("hello world" + " GitHub.\n"); // warning
    }
}
