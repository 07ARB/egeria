/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.frameworks.connectors.properties;

import org.odpi.openmetadata.frameworks.connectors.ffdc.OCFErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.OCFRuntimeException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.ElementBase;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.SchemaAttribute;

import java.util.Iterator;

/**
 * SchemaAttributes supports an iterator over a list of schema attribute elements that make up a schema.
 * Callers can use it to step through the list
 * just once.  If they want to parse the list again, they could use the copy/clone constructor to create
 * a new iterator.
 */
public abstract class SchemaAttributes extends PropertyIteratorBase implements Iterator<SchemaAttribute>
{
    private static final long     serialVersionUID = 1L;

    /**
     * Typical Constructor creates an iterator with the supplied list of elements.
     *
     * @param totalElementCount the total number of elements to process.  A negative value is converted to 0.
     * @param maxCacheSize maximum number of elements that should be retrieved from the property server and
     *                     cached in the element list at any one time.  If a number less than one is supplied, 1 is used.
     */
    public SchemaAttributes(int totalElementCount,
                            int maxCacheSize)
    {
        super(totalElementCount, maxCacheSize);
        // Anonymous class for temporary changing the behaviour of the iterator in the case of SchemaAttributes
        // as to not use the "totalElementCount" parameter because its value is always 0
        super.pagingIterator = new PagingIterator(this, totalElementCount, maxCacheSize)
        {
            @Override
            public ElementBase next()
            {
                    if (this.hasNext())
                    {
                        ElementBase retrievedElement = iterator.cloneElement(cachedElementList.get(cachedElementPointer));
                        cachedElementPointer++;
                        cachedElementStart++;

                        log.debug("Returning next element:");
                        log.debug("==> totalElementCount: " + totalElementCount);
                        log.debug("==> cachedElementPointer: " + cachedElementPointer);
                        log.debug("==> cachedElementStart:" + cachedElementStart);
                        log.debug("==> maxCacheSize:" + maxCacheSize);

                        return retrievedElement;
                    }
                    else
                    {
                        /*
                         * Throw runtime exception to show the caller they are not using the list correctly.
                         */
                        throw new OCFRuntimeException(OCFErrorCode.NO_MORE_ELEMENTS.getMessageDefinition(this.getClass().getName()),
                                                                                                         this.getClass().getName(),
                                                                                                         "next");
                    }
            }

            @Override
            public boolean hasNext()
            {
                if (cachedElementList == null)
                {
                    return false;
                }

                if (cachedElementPointer == cachedElementList.size())
                {
                    try
                    {
                        cachedElementList = iterator.getCachedList(cachedElementStart, maxCacheSize);
                        if (cachedElementList == null)
                        {
                            return false;
                        }

                        cachedElementPointer = 0;
                    }
                    catch (PropertyServerException error)
                    {
                        /*
                         * Problem retrieving next cache.  The exception includes a detailed error message,
                         */
                        throw new OCFRuntimeException(OCFErrorCode.PROPERTIES_NOT_AVAILABLE.getMessageDefinition(error.getReportedErrorMessage(),
                                                                                                                 this.toString()),
                                                                                                                 this.getClass().getName(),
                                                                                                                 "next",
                                                                                                                 error);
                    }
                }

                return true;
            }
        };
    }


    /**
     * Copy/clone constructor.  Used to reset iterator element pointer to 0;
     *
     * @param template type-specific iterator to copy; null to create an empty iterator
     */
    public SchemaAttributes(SchemaAttributes template)
    {
        super(template);
    }


    /**
     * Provides a concrete implementation of cloneElement for the specific iterator type.
     *
     * @param template object to clone
     * @return new cloned object.
     */
    protected ElementBase cloneElement(ElementBase template)
    {
        if (template instanceof SchemaAttribute)
        {
            return new SchemaAttribute((SchemaAttribute)template);
        }

        return null;
    }


    /**
     * Clones this iterator.
     *
     * @return new cloned object.
     */
    protected  abstract SchemaAttributes cloneIterator();


    /**
     * The iterator can only be used once to step through the elements.  This method returns
     * a boolean to indicate if it has got to the end of the list yet.
     *
     * @return boolean indicating whether there are more elements.
     */
    @Override
    public boolean hasNext()
    {
        return super.pagingIterator.hasNext();
    }


    /**
     * Return the next element in the iteration.
     *
     * @return SchemaAttribute next element object that has been cloned.
     */
    @Override
    public SchemaAttribute next()
    {
        return (SchemaAttribute)super.pagingIterator.next();
    }


    /**
     * Remove the current element in the iterator. (Null implementation since this iterator works off of cached
     * elements from the property (metadata) server.)
     */
    @Override
    public void remove()
    {
        throw new OCFRuntimeException(OCFErrorCode.UNABLE_TO_REMOVE.getMessageDefinition(this.getClass().getName()),
                                      this.getClass().getName(),
                                      "remove");
    }


    /**
     * Standard toString method.
     *
     * @return print out of variables in a JSON-style
     */
    @Override
    public String toString()
    {
        return "SchemaAttributes{" +
                "pagingIterator=" + pagingIterator +
                '}';
    }
}