package com.example.scowluga.mycalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

public class RegularCalc extends AppCompatActivity {
    public static String memory = "0"; //storage for the memory function of calculator
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regular_calc);

        final TextView expression; // textview with expression
        expression = (TextView) findViewById(R.id.textView);
        final TextView answer = (TextView)findViewById(R.id.answer); // textview with answer
        nullText(expression, answer);

        GridLayout grid = (GridLayout)findViewById(R.id.gridLayout); //grid layout with buttons
        gridListen(grid, expression, answer);
    }

    private void nullText(TextView expression, TextView answer) {
        expression.setText(""); // clear the text values that are to help us in layout
        answer.setText("");
    }

    private void gridListen(GridLayout grid, final TextView expression, final TextView answer) {

        for (int i = 0 ; i < grid.getChildCount(); i ++) { //iterate through gridlayout
            final Button b = (Button)grid.getChildAt(i); //find the button

            b.setOnClickListener(new View.OnClickListener() { //set listener
                @Override
                public void onClick(View v) {
                    click(expression, answer, b); //run a function
                }
            });
        }
    }
    static void click (TextView express, TextView end, Button b) { //on a button's click
        String operation = b.getText().toString(); //get the operator (the button's text)
        String current = express.getText().toString(); //get the current text in expression

        switch (operation) {
            case "M+":
                memory = end.getText().toString(); //store the last calculated answer into memory
                break;
            case "=":
                // /special case, replaces "expression" with answer and continues on
                String ans; // to find value of expression
                try {
                    Double result = eval(current); //get number from eval fx
                    ans = result.toString(); //get string
                } catch (Exception e) { //CATCH EXCEPTION THROWN BY EVAL FUNCTION
                    ans = "---"; //set answer of "error"
                }
                update(express, end, ans); //plugging in the current expression as the answer
                break;
            case "MR":
                current = current + memory; //add the memory to current
                update(express, end, current);
                break;
            case "C":
                current = ""; //clear
                update(express, end, current);
                break;
            default: //ANY NORMAL BUTTON
                current += operation; //adding w/e button was pressed
                update(express, end, current);
        }
    }; // called on click

    static void update (TextView express, TextView end, String current) {
        String ans; // to update end textview's text with ans
        try {
            Double result = eval(current); //get number from eval fx
            ans = result.toString(); //get string
        } catch (Exception e) { //CATCH EXCEPTION THROWN BY EVAL FUNCTION
            ans = "---"; //set answer of "error"
        }

        express.setText(current); //Updating the current expression
        end.setText(ans); //Updating the current result of expression

    }; // update button's text

    // Copied from stack overflow post
    // http://stackoverflow.com/questions/3422673/evaluating-a-math-expression-given-in-string-form
    static double eval(final String str) { //evaluate, or else return error
        return new Object() {
            int pos = -1, ch;
            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    } // evaluate
}
