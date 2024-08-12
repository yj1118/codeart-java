package apros.codeart.dto;

import apros.codeart.util.ListUtil;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings({"exports"})
class ReadObjectArrayMember {

    public static class User {
        private int _id;

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            _id = id;
        }

        public long[] roleIds;

        public User(int id) {
            _id = id;
        }
    }

    @Test
    public void common() {
        var dto = DTObject.editable();
        dto.setLong("id", 1);

        var ids = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        dto.pushLongs("roleIds", ids);

        var user = dto.save(User.class);

        assertEquals(1, user._id);
        assertEquals(9, user.roleIds.length);
    }


}
