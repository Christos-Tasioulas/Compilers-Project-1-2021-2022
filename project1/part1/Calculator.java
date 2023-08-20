import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Calculator 
{
    public static void main(String[] args) throws IOException, ParseError
    {
        if (args.length != 1) 
        {
            System.err.println("Invalid number of arguments");
            System.exit(1);
        } 

        BufferedReader in = null;
        try 
        {
            in = new BufferedReader(new FileReader(new File(args[0])));

            String exp;
            // Reading every line from the input
            while ((exp = in.readLine()) != null) 
            {
                /* 
                    main program reading the mathematical expression, parsing and evaluating its result 
                */
                Expression expression = new Expression(exp);
                expression.parse();
                System.out.println("\nEvaluated Result: " + expression.get_evaluated_result());   
            }
        } 
        catch (FileNotFoundException e) 
        {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch(IOException e)
            {
                System.err.println(e.getMessage());
                System.exit(2);
            }
        }
    }

    
}

/*
    This is where the mathematical expressions are processed
    It contains:
    > the expression itself
    > the expression's character we are currently processing
    > its index
    > a stack where we store characters we processed. We use it to evaluate the result the characters inside make.
    > the result we found from the expression's character using the aforementioned stack  
*/
class Expression
{
    private static String expression; 
    private static int current_index;
    private static String current_character;
    private static Stack<String> computables;
    private static int current_result;

    // Constructor
    Expression(String exp)
    {
        expression = exp;
        current_index = 0;
        computables = new Stack<>();
        read_character();
    }

    // Returns the result we evaluated
    int get_evaluated_result()
    {
        return current_result;
    }

    // Moving onto the next character of the expression
    private static void advance()
    {
        current_index++;
        read_character();
    }

    /*
        Reading and saving the character in the current index position we stored
    */
    private static void read_character()
    {
        // if the current index is greater than the expression's characters we store the EOF character "$"
        /*
            This is highly likely to happen since the advance function above could be called more times than we need it
            from the recursive parse functions. An instance which the "ε" appears on the expression's tree. 
        */
        if(current_index >= expression.length()) current_character = "$";
        else
        {
            char c = expression.charAt(current_index);
            current_character = String.valueOf(c);
            System.out.print(current_character);
        }
        
    }

    // Evaluating the result of the part of the expression inside the stack
    private static void evaluate()
    {
        String term1 = computables.pop();

        // case that only a number exists inside the stack 
        if(computables.empty())
        {
            int num = Integer.parseInt(term1);
            current_result = num;
            computables.push(term1);
            return;
        }

        String op = computables.pop();
        String term2 = computables.pop();

        int num_1 = Integer.parseInt(term1);
        int num_2 = Integer.parseInt(term2);
        int result;

        if(op.equals("^")) result = num_1 ^ num_2;  // A & B
        else if(op.equals("&")) result = num_1 & num_2; // A ^ B
        // There must have been a mistake
        else
        {
            System.out.print("cannot evaluate the result of this expression");
            return;
        } 

        // The current result might be used for later evaluations 
        computables.push(String.valueOf(result));
        // Or it could be the final one
        current_result = result; 
    }

    // Non static way to call the parsing functions
    void parse() throws IOException, ParseError
    {
        expr();
    }

    /* 
        expr → term expr0 
    */
    private static void expr() throws IOException, ParseError 
    {
        // FIRST+(expr) = {(, num}, num ∈ [0, 9]
        Set<String> first_plus_term = First_plus("term");

        // expr will start only with a single digit number or '(' if correct
        if(first_plus_term.contains(current_character))
        {
            // expr → term expr0
            term();
            expr0();

            return;
            
        }
        // parse error
        throw new ParseError();

    }

    /* 
        expr0 → ^ term expr0 
        expr0 → ε
    */
    private static void expr0() throws IOException, ParseError
    {
        // FIRST+(expr0) = {^, ), ε}
        Set<String> first_plus_expr0 = First_plus("expr0");

        // expr0 will start only with '^' or end with ')' or it is blank with EOF or ε if correct
        if(first_plus_expr0.contains(current_character))
        {
            // expr0 → ^ term expr0
            if(current_character.equals("^")) 
            {
                // the operator will be used to evaluate temporar result of the expression
                computables.push("^");

                // next character
                advance();

                // expr0 → ^ term expr0
                term();
                expr0();

                // evaluating result for the terms between the current operator 
                evaluate();
            }
            // expr0 → ε , if line 201 not true
            return;
        }
        // parse error
        throw new ParseError();
    }

    /* 
        term → term0 expr1
    */
    private static void term() throws IOException, ParseError
    {
        // FIRST+(term) = {(, num}, num ∈ [0, 9]
        Set<String> first_plus_term0 = First_plus("term0");

        // term will start only with a single digit number or '(' if correct
        if(first_plus_term0.contains(current_character))
        {
            // term → term0 expr1
            term0();
            expr1();
            
            return;
        }
        // parse error
        throw new ParseError();
    }

    /* 
        expr1 → & term0 expr1 
        expr1 → ε
        returns true if everything is ok and false if there is a parse error
    */
    private static void expr1() throws IOException, ParseError
    {
        // FIRST+(expr1) = {^, &, ), ε}
        Set<String> first_plus_expr1 = First_plus("expr1");

        // expr1 will start only with '&' or end with ')' or '^' or it is blank with EOF or ε if correct
        if(first_plus_expr1.contains(current_character))
        {
            if(current_character.equals("&")) 
            {
                // the operator will be used to evaluate temporar result of the expression
                computables.push("&");
                // next character
                advance();

                // expr1 → & term0 expr1
                term0();
                expr1();

                // evaluating result for the terms between the current operator
                evaluate();
            }
            // expr1 → ε , if line 255 not true
            return;
        }
        // parse error
        throw new ParseError();
    }

    /* 
        term0 → (expr)
        term0 → num
        returns true if everything is ok and false if there is a parse error
    */
    private static void term0() throws IOException, ParseError
    {
        // FIRST+(term0) = {(, num}, num ∈ [0, 9]
        Set<String> first_plus_term0 = First_plus("term0");

        // term will start only with a single digit number or '(' if correct
        if(first_plus_term0.contains(current_character))
        {
            // we have opened a parenthesis
            if(current_character.equals("("))
            {
                // next character
                advance();

                // term0 → (expr)
                expr();

                if(current_character.equals(")")) 
                {
                    // evaluating the result of everything inside the parenthesis
                    evaluate();

                    // we are closing the parenthesis and we are moving on
                    advance();

                    return;
                }

                // parse error: parenthesis left open
                throw new ParseError(); 
            }
            // term0 → num
            else
            {
                // the number is inserted into the stack to evaluated its result with the other items in the stack
                computables.push(current_character);

                // next character
                advance();

                return;
            }
        }
        // parse error
        throw new ParseError();
    }

    // returns a set of strings(chars) that could either percede or be anywhere near the expression given 
    static Set<String> First_plus(String type)
    {
        /* 
            FIRST+(A → α) as FIRST(α) U FOLLOW(A), if ε ∈ FIRST(α) or FIRST(α), otherwise
        */ 
        
        Set<String> first = First(type);

        if(first.contains(" ")) 
        {
            Set<String> follow = Follow(type);
            first.addAll(follow);
        }
        
        return first;
    }

    // returns a set of strings(chars) that could follow the token given
    static Set<String> Follow(String type)
    {
        Set<String> follow = new HashSet<String>();

        // Place $ in FOLLOW(S), where S is the start symbol and $ is the input right endmarker
        follow.add("$");

        if(type.equals("expr"))
        {
            // expr can be followed by ')' (base case)
            follow.add(")");
            return follow;
        }
        else if(type.equals("term"))
        {
            /*
                -If there is a production expr0 → ^ term expr0, then everything in FIRST(expr0), except for ε, is placed in FOLLOW(term).
                -If there is a production expr → term expr0, or a production expr0 → ^ term expr0 where FIRST(expr0) contains ε (i.e., expr0 → ε),
                then everything in FOLLOW(expr0) is in FOLLOW(term).
            */
            type = "expr0";
            Set<String> first = First(type);
            first.remove(" ");
            follow = Follow(type);
            follow.addAll(first);
            return follow;
        }
        else if(type.equals("expr0"))
        {
            // If there is a production expr → term expr0, then everything in FOLLOW(expr) is in FOLLOW(expr0).
            type = "expr";
            return Follow(type);
        }
        else if(type.equals("term0"))
        {
            /*
                -If there is a production expr1 → & term0 expr1, then everything in FIRST(expr1), except for ε, is placed in FOLLOW(term0).
                -If there is a production term → term0 expr1, or a production expr1 → & term0 expr1 where FIRST(expr1) contains ε (i.e., expr1 → ε),
                then everything in FOLLOW(expr1) is in FOLLOW(term0).
            */
            type = "expr1";
            Set<String> first = First(type);
            first.remove(" ");
            follow = Follow(type);
            follow.addAll(first);
            return follow;   
        }
        else if(type.equals("expr1"))
        {
            // If there is a production term → term0 expr1, then everything in FOLLOW(term) is in FOLLOW(expr1).
            type = "term";
            return Follow(type);
        }

        return follow;
    }

    // returns a set of strings(chars) that could be found before the token given
    private static Set<String> First(String type)
    {
        Set<String> first = new HashSet<String>();

        if(type.equals("expr"))
        {
            /*
                expr → term expr0
                therefore
                FIRST(expr) = FIRST(term)
            */
            type = "term";
            return First(type);
        }
        else if(type.equals("term"))
        {
            /*
                term → term0 expr1
                therefore
                FIRST(term) = FIRST(term0)
            */
            type = "term0";
            return First(type);
        }
        else if(type.equals("expr0"))
        {
            /*
                expr0 → ^ term expr0 | ε
                therefore
                FIRST(expr0) = {^, ε} 
            */
            // If expr0 → ε is a production, then add ε to FIRST(expr0).
            first.add("^");
            first.add(" ");
            return first;
        }
        else if(type.equals("term0"))
        {
            /*
                term0 → (expr) | num
                therefore
                FIRST(term0) = {(, num} 
            */
            first.add("(");
            // adding all single digit numbers
            first.add("0");
            first.add("1");
            first.add("2");
            first.add("3");
            first.add("4");
            first.add("5");
            first.add("6");
            first.add("7");
            first.add("8");
            first.add("9");
            return first;
        }
        else if(type.equals("expr1"))
        {
            /*
                expr1 → & term0 expr1 | ε
                therefore
                FIRST(expr1) = {&, ε} 
            */
            // If expr1 → ε is a production, then add ε to FIRST(expr1).
            first.add("&");
            first.add(" ");
            return first;
        }  
        
        return first;
    }
}