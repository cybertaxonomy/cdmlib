package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import org.hibernate.search.spatial.impl.Point;
import org.hibernate.search.spatial.impl.Rectangle;
import org.springframework.util.Assert;

/**
 * BBOX=minx(minlongitute),miny(minlatitute),maxx(maxlongitute),max(maxlatitute): Bounding box corners (lower left, upper right)
 *
 * @author a.kohlbecker
 * @since Apr 26, 2013
 *
 */
public class RectanglePropertyEditor extends PropertyEditorSupport {

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
     */
    @Override
    public void setAsText(String text) {
        String[] values = text.split(",");
        Assert.isTrue(values.length == 4, "A rectangle string must contain four values");
        setValue(new Rectangle(
                // Points are constructed as : latitude, longitude
                Point.fromDegreesInclusive(Double.parseDouble(values[1]), Double.parseDouble(values[0])),
                Point.fromDegreesInclusive(Double.parseDouble(values[3]), Double.parseDouble(values[2]))
              ));
    }

}
