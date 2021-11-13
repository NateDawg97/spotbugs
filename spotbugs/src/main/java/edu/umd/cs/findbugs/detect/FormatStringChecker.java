/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2004-2006 University of Maryland
 * Copyright (C) 2008 Google
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.detect;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.StringAnnotation;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class FormatStringChecker extends OpcodeStackDetector {

    final BugReporter bugReporter;

    public FormatStringChecker(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    enum FormatState {
        NONE, READY_FOR_FORMAT, EXPECTING_ASSIGNMENT
    }

    FormatState state;

    String formatString;

    int stackDepth;

    OpcodeStack.Item arguments[];

    @Override
    public void visit(Code code) {
        state = FormatState.NONE;
        super.visit(code); // make callbacks to sawOpcode for all opcodes
        arguments = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
     */
    @Override
    public void sawOpcode(int seen) {
        System.out.println(getPC() + " " + Const.getOpcodeName(seen) + " " + state);

        if (stack.getStackDepth() < stackDepth) {
            state = FormatState.NONE;
            stackDepth = 0;
            arguments = null;
        }

        //String cl2 = getClassConstantOperand();
        //String nm2 = getNameConstantOperand();
        //String sig2 = getSigConstantOperand();
        //XMethod m2 = getXMethodOperand();

        this.printOpCode(seen);
        //System.out.println("Hello, CL: " + cl2 + ", NM: " + nm2 + ", SIG: " + sig2 + ", XM: " + m2);

        if (seen == Const.ANEWARRAY && stack.getStackDepth() >= 2) {
            System.out.println("A New Array???");
            Object size = stack.getStackItem(0).getConstant();
            Object formatStr = stack.getStackItem(1).getConstant();

            int tempStackDepth = stack.getStackDepth();

            for (int i = 0; i < tempStackDepth; i++) {
                System.out.println("\tStack Item: " + stack.getStackItem(i));
            }

            System.out.println("STACK DEPTH: " + stack.getStackDepth());

            if (size instanceof Integer && formatStr instanceof String) {
                arguments = new OpcodeStack.Item[(Integer) size];
                this.formatString = (String) formatStr;
                System.out.println("SETTING READY_FOR_FORMAT: " + formatStr + ", " + size);
                state = FormatState.READY_FOR_FORMAT;
                stackDepth = stack.getStackDepth();
            }
        } else if (state == FormatState.READY_FOR_FORMAT && seen == Const.DUP) {
            //System.out.println("Ready for Format???");
            state = FormatState.EXPECTING_ASSIGNMENT;
        } else if (state == FormatState.EXPECTING_ASSIGNMENT && stack.getStackDepth() == stackDepth + 3 && seen == Const.AASTORE) {
            //System.out.println("Expecting Assignment???");
            Object pos = stack.getStackItem(1).getConstant();
            OpcodeStack.Item value = stack.getStackItem(0);

            if (pos instanceof Integer) {
                int index = (Integer) pos;
                if (index >= 0 && index < arguments.length) {
                    arguments[index] = value;
                    state = FormatState.READY_FOR_FORMAT;
                } else {
                    state = FormatState.NONE;
                }
            } else {
                state = FormatState.NONE;
            }
        } else if (state == FormatState.READY_FOR_FORMAT
                && (seen == Const.INVOKESPECIAL || seen == Const.INVOKEVIRTUAL || seen == Const.INVOKESTATIC || seen == Const.INVOKEINTERFACE)
                && stack.getStackDepth() == stackDepth) {

            String cl = getClassConstantOperand();
            String nm = getNameConstantOperand();
            String sig = getSigConstantOperand();
            XMethod m = getXMethodOperand();

            System.out.println("Hello, CL: " + cl + ", NM: " + nm + ", SIG: " + sig);

            if ((m == null || m.isVarArgs())
                    && sig.indexOf("Ljava/lang/String;[Ljava/lang/Object;)") >= 0
                    && ("java/util/Formatter".equals(cl) && "format".equals(nm) || "java/lang/String".equals(cl)
                            && "format".equals(nm) || "java/io/PrintStream".equals(cl) && "format".equals(nm)
                            || "java/io/PrintStream".equals(cl) && "printf".equals(nm) || cl.endsWith("Writer")
                                    && "format".equals(nm) || cl.endsWith("Writer") && "printf".equals(nm)) || cl.endsWith("Logger")
                                            && nm.endsWith("fmt")) {

                if (formatString.indexOf('\n') >= 0) {
                    //System.out.println("POOP!!!");
                    bugReporter.reportBug(new BugInstance(this, "VA_FORMAT_STRING_USES_NEWLINE", NORMAL_PRIORITY)
                            .addClassAndMethod(this).addCalledMethod(this).addString(formatString)
                            .describe(StringAnnotation.FORMAT_STRING_ROLE).addSourceLine(this));
                }
            }

        }
    }

}
