/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
require(['jquery', 'deferred!ckeditor',], function ($, ckeditorPromise) {
    function activated(editor) {
      let ascendant = editor.getSelection().getStartElement()
        .getAscendant(element => {
          if (!element.getAttribute) {
            return false;
          }
          let attribute = element.getAttribute("class");
          let split = attribute?.split(' ');
          return split?.includes('paragraphs-numbering-root')
        }, true);
      return ascendant !== null;
    }

    ckeditorPromise.done(ckeditor => {
      if (!('xwiki-numberedParagraphs' in ckeditor.plugins.registered)) {
        ckeditor.plugins.add('xwiki-numberedParagraphs', {
          init: function (editor) {
            handleEnterKey(editor)
          }
        });

        ckeditor.on('instanceCreated', event => {
          if (event.editor.config.extraPlugins === '') {
            event.editor.config.extraPlugins = 'xwiki-numberedParagraphs';
          } else {
            event.editor.config.extraPlugins += ',xwiki-numberedParagraphs';
          }
        });
      }

      //
      // Enter specific plugin.
      //
      const handleEnterKey = function (editor) {
        editor.on('afterCommandExec', event => {
          if (event.data.name === 'enter') {
            afterEnterKey(editor);
          }
        });
      };

      const afterEnterKey = function (editor) {
        const selection = editor.getSelection();
        // We intercept the Enter key only if the selection is collapsed, and numbered content is activated.
        if (selection.isCollapsed() && activated(editor)) {
          const parentBlock = selection.getStartElement()
            .getAscendant(element => ckeditor.dtd.$block[element.getName()], true);
          const previousBlock = parentBlock.getPrevious();
          if (parentBlock.is('p') && /h[1-6]/.test(previousBlock.getName())) {
            if (previousBlock.getText().trim().length) {
              const bookmarks = selection.createBookmarks2();
              parentBlock.renameNode(previousBlock.getName());
              selection.selectBookmarks(bookmarks);
            } else {
              previousBlock.remove();
            }
          }
        }
      };
    });

  }
)
