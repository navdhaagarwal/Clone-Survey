package com.nucleus.core.validation.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.nucleus.core.exceptions.InvalidDataException;

public class NeutrinoValidator {

    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise 
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating an 
     * object or using your own custom validation expression.</p>
     *
     * @param expression the boolean expression to check 
     * @param message the exception message if invalid
     * @param value the value to append to the message when invalid
     * @throws InvalidDataException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message, Object value) {
        if (expression == false) {
            throw new InvalidDataException(message + value);
        }
    }

    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise 
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a 
     * primitive number or using your own custom validation expression.</p>
     * 
     * @param expression the boolean expression to check 
     * @param message the exception message if invalid
     * @param value the value to append to the message when invalid
     * @throws InvalidDataException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message, long value) {
        if (expression == false) {
            throw new InvalidDataException(message + value);
        }
    }

    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise 
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a 
     * primitive number or using your own custom validation expression.</p>
     * 
     * @param expression the boolean expression to check 
     * @param message the exception message if invalid
     * @param value the value to append to the message when invalid
     * @throws InvalidDataException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message, double value) {
        if (expression == false) {
            throw new InvalidDataException(message + value);
        }
    }

    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise 
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a 
     * primitive number or using your own custom validation expression.</p>
     *
     * @param expression the boolean expression to check 
     * @param message the exception message if invalid
     * @throws InvalidDataException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message) {
        if (expression == false) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise 
     * throwing an exception. This method is useful when validating according 
     * to an arbitrary boolean expression, such as validating a 
     * primitive number or using your own custom validation expression.</p>
     *
     * @param expression the boolean expression to check 
     * @throws InvalidDataException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression) {
        if (expression == false) {
            throw new InvalidDataException("The validated expression is false");
        }
    }

    /**
     * <p>Validate that the specified argument is not <code>null</code>; 
     * otherwise throwing an exception.
     * 
     * @param object the object to check
     * @throws InvalidDataException if the object is <code>null</code>
     */
    public static void notNull(Object object) {
        notNull(object, "The validated object is null");
    }

    /**
     * <p>Validate that the specified argument is not <code>null</code>; 
     * otherwise throwing an exception with the specified message.
     * 
     * @param object the object to check
     * @param message the exception message if invalid
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * <p>Validate that the specified argument array is neither <code>null</code> 
     * nor a length of zero (no elements); otherwise throwing an exception 
     * with the specified message.
     * 
     * @param array the array to check
     * @param message the exception message if invalid
     * @throws InvalidDataException if the array is empty
     */
    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * <p>Validate that the specified argument array is neither <code>null</code> 
     * nor a length of zero (no elements); otherwise throwing an exception. 
     *
     * @param array the array to check
     * @throws InvalidDataException if the array is empty
     */
    public static void notEmpty(Object[] array) {
        notEmpty(array, "The validated array is empty");
    }

    /**
     * <p>Validate that the specified argument collection is neither <code>null</code> 
     * nor a size of zero (no elements); otherwise throwing an exception 
     * with the specified message.
     * 
     * @param collection the collection to check
     * @param message the exception message if invalid
     * @throws InvalidDataException if the collection is empty
     */
    public static void notEmpty(Collection collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * <p>Validate that the specified argument collection is neither <code>null</code> 
     * nor a size of zero (no elements); otherwise throwing an exception. 
     *
     * @param collection the collection to check
     * @throws InvalidDataException if the collection is empty
     */
    public static void notEmpty(Collection collection) {
        notEmpty(collection, "The validated collection is empty");
    }

    /**
     * <p>Validate that the specified argument map is neither <code>null</code> 
     * nor a size of zero (no elements); otherwise throwing an exception 
     * with the specified message.
     *
     * @param map the map to check
     * @param message the exception message if invalid
     * @throws InvalidDataException if the map is empty
     */
    public static void notEmpty(Map map, String message) {
        if (map == null || map.isEmpty()) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * <p>Validate that the specified argument map is neither <code>null</code> 
     * nor a size of zero (no elements); otherwise throwing an exception. 
     *
     * @param map the map to check
     * @throws InvalidDataException if the map is empty
     * @see #notEmpty(Map, String)
     */
    public static void notEmpty(Map map) {
        notEmpty(map, "The validated map is empty");
    }

    /**
     * <p>Validate that the specified argument string is 
     * neither <code>null</code> nor a length of zero (no characters); 
     * otherwise throwing an exception with the specified message.
     *
     * @param string the string to check
     * @param message the exception message if invalid
     * @throws InvalidDataException if the string is empty
     */
    public static void notEmpty(String string, String message) {
        if (string == null || string.length() == 0) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * <p>Validate that the specified argument string is 
     * neither <code>null</code> nor a length of zero (no characters); 
     * otherwise throwing an exception with the specified message.
     *
     * @param string the string to check
     * @throws InvalidDataException if the string is empty
     */
    public static void notEmpty(String string) {
        notEmpty(string, "The validated string is empty");
    }

    /**
     * <p>Validate that the specified argument array is neither 
     * <code>null</code> nor contains any elements that are <code>null</code>;
     * otherwise throwing an exception with the specified message.
     *
     * @param array the array to check
     * @param message the exception message if the collection has <code>null</code> elements
     * @throws InvalidDataException if the array is <code>null</code> or
     * an element in the array is <code>null</code>
     */
    public static void noNullElements(Object[] array, String message) {
        notNull(array);
        for (int i = 0 ; i < array.length ; i++) {
            if (array[i] == null) {
                throw new InvalidDataException(message);
            }
        }
    }

    /**
     * <p>Validate that the specified argument array is neither 
     * <code>null</code> nor contains any elements that are <code>null</code>;
     * otherwise throwing an exception.
     *
     * @param array the array to check
     * @throws InvalidDataException if the array is <code>null</code> or
     * an element in the array is <code>null</code>
     */
    public static void noNullElements(Object[] array) {
        notNull(array);
        for (int i = 0 ; i < array.length ; i++) {
            if (array[i] == null) {
                throw new InvalidDataException("The validated array contains null element at index: " + i);
            }
        }
    }

    /**
     * <p>Validate that the specified argument collection is neither 
     * <code>null</code> nor contains any elements that are <code>null</code>;
     * otherwise throwing an exception with the specified message.
     *
     * @param collection  the collection to check
     * @param message  the exception message if the collection has
     * @throws InvalidDataException if the collection is <code>null</code> or
     * an element in the collection is <code>null</code>
     */
    public static void noNullElements(Collection collection, String message) {
        notNull(collection);
        for (Iterator it = collection.iterator() ; it.hasNext() ;) {
            if (it.next() == null) {
                throw new InvalidDataException(message);
            }
        }
    }

    /**
     * <p>Validate that the specified argument collection is neither 
     * <code>null</code> nor contains any elements that are <code>null</code>;
     * otherwise throwing an exception.
     *
     * @param collection  the collection to check
     * @throws InvalidDataException if the collection is <code>null</code> or
     * an element in the collection is <code>null</code>
     */
    public static void noNullElements(Collection collection) {
        notNull(collection);
        int i = 0;
        Iterator it = collection.iterator();
        while(it.hasNext()){
            if (it.next() == null) {
                throw new InvalidDataException("The validated collection contains null element at index: " + i);
            }
            i++;
        }
    }

    /**
     * <p>Validate an argument, throwing <code>InvalidDataException</code>
     * if the argument collection  is <code>null</code> or has elements that
     * are not of type <code>clazz</code> or a subclass.</p>
     *
     * @param collection  the collection to check, not null
     * @param clazz  the <code>Class</code> which the collection's elements are expected to be, not null
     * @param message  the exception message if the <code>Collection</code> has elements not of type <code>clazz</code>
     * @since 2.1
     */
    public static void allElementsOfType(Collection collection, Class clazz, String message) {
        notNull(collection);
        notNull(clazz);
        for (Iterator it = collection.iterator() ; it.hasNext() ;) {
            if (clazz.isInstance(it.next()) == false) {
                throw new InvalidDataException(message);
            }
        }
    }

    /**
     * <p>
     * Validate an argument, throwing <code>InvalidDataException</code> if the argument collection is
     * <code>null</code> or has elements that are not of type <code>clazz</code> or a subclass.
     * </p>
     *  
     * @param collection  the collection to check, not null
     * @param clazz  the <code>Class</code> which the collection's elements are expected to be, not null
     */
    public static void allElementsOfType(Collection collection, Class clazz) {
        notNull(collection);
        notNull(clazz);
        int i = 0;
        Iterator it = collection.iterator();
        while(it.hasNext()) {
            if (!clazz.isInstance(it.next())) {
                throw new InvalidDataException("The validated collection contains an element not of type " + clazz.getName()
                        + " at index: " + i);
            }
            i++;
        }
    }

    /**
     * Validate that the given String has valid text content; that is, it must not
     * be <code>null</code> and must contain at least one non-whitespace character.
     * 
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     */
    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new InvalidDataException(message);
        }
    }

    /**
     * Validate that the given String has valid text content; that is, it must not
     * be <code>null</code> and must contain at least one non-whitespace character.
     * 
     * @param text the String to check
     * @see StringUtils#hasText
     */
    public static void hasText(String text) {
        hasText(text, "This String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * Validate that the provided object is an instance of the provided class.
     * 
     * @param clazz the required class
     * @param obj the object to check
     * @throws InvalidDataException if the object is not an instance of clazz
     */
    public static void isInstanceOf(Class clazz, Object obj) {
        isInstanceOf(clazz, obj, "");
    }

    /**
     * Validate that the provided object is an instance of the provided class.
     *  
     * @param type the type to check against
     * @param obj the object to check
     * @param message a message which will be prepended to the message produced by
     * the function itself, and which may be used to provide context. It should
     * normally end in a ": " or ". " so that the function generate message looks
     * ok when prepended to it.
     * @throws InvalidDataException if the object is not an instance of clazz
     */
    public static void isInstanceOf(Class type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new InvalidDataException(message + "Object of class [" + (obj != null ? obj.getClass().getName() : "null")
                    + "] must be an instance of " + type);
        }
    }

    public static void isPasswordFormatCorrect(String password, String regExp, String regExpDesc) {
        if (!password.matches(regExp)) {
            throw new InvalidDataException(regExpDesc);
        }
    }

}
