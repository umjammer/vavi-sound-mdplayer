/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 *  Copyright (C) 2011-2015 Leandro Nini
 *  Copyright (C) 2009 Antti S. Lankila
 *  Copyright (C) 2001 Simon White
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mdplayer.driver.sid.libsidplayfp;

public class EventCallback<This> extends Event {

    public interface Callback extends Runnable {}

    private This _this;

    public Callback callback;

    @Override
    public void event() {
        callback.run();
    }

    /**
     * 注意：Callback は object インスタンスのメソッドをセットすること
     * @param name
     * @param object
     * @param callback
     */
    public EventCallback(String name, This object, Callback callback) {
        super(name);
        _this = object;
        this.callback = callback;
    }
}
