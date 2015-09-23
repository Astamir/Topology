package serialization.marshall;

import ru.etu.astamir.model.contacts.ContactType;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public class LegacyContactTypeMarshallTest extends EnumJAXBTest<ContactType> {
    public LegacyContactTypeMarshallTest() {
        super(ContactType.class);
    }
}
