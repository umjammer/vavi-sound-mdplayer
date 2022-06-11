/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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

//template< class This >
public class EventCallback<This> extends Event {

    // # include "Event.h"
    // # include "sidcxx11.h"

    public interface Callback extends Runnable {}

    private This m_this;

    public Callback m_callback;

    @Override
    public void event_() {
        m_callback.run();
    }

    /**
     * 注意：callbackはObject_インスタンスのメソッドをセットすること
     */
    // <param name="name"></param>
    // <param name="Object_"></param>
    // <param name="callback"></param>
    public EventCallback(String name, This Object_, Callback callback) {
        super(name);
        m_this = Object_;
        m_callback = callback;
    }
}
