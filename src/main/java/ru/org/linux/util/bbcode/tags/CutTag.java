/*
 * Copyright 1998-2016 Linux.org.ru
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * Copyright (c) 2005-2006, Luke Plant
 * All rights reserved.
 * E-mail: <L.Plant.98@cantab.net>
 * Web: http://lukeplant.me.uk/
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above
 *        copyright notice, this list of conditions and the following
 *        disclaimer in the documentation and/or other materials provided
 *        with the distribution.
 *
 *      * The name of Luke Plant may not be used to endorse or promote
 *        products derived from this software without specific prior
 *        written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Rewrite with Java language and modified for lorsource by Ildar Hizbulin 2011
 * E-mail: <hizel@vyborg.ru>
 */

package ru.org.linux.util.bbcode.tags;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.httpclient.URI;
import ru.org.linux.util.bbcode.NodeUtils;
import ru.org.linux.util.bbcode.Parser;
import ru.org.linux.util.bbcode.ParserParameters;
import ru.org.linux.util.bbcode.nodes.Node;
import ru.org.linux.util.bbcode.nodes.RootNode;
import ru.org.linux.util.bbcode.nodes.TagNode;
import ru.org.linux.util.formatter.ToHtmlFormatter;

public class CutTag extends HtmlEquivTag {

  public CutTag(ImmutableSet<String> allowedChildren, ParserParameters parserParameters) {
    super("cut", allowedChildren, "div", parserParameters, "div");
  }

  @Override
  public String renderNodeXhtml(Node node) {
    if(NodeUtils.isEmptyNode(node)) {
      return "";
    }
    if (!node.isParameter()) {
      node.setParameter("");
    } else {
      node.setParameter(node.getParameter().trim());
    }
    TagNode tagNode = (TagNode)node;
    RootNode rootNode = tagNode.getRootNode();
    if (rootNode.isComment()) { // коментарий, просто содержимое
      return node.renderChildrenXHtml();
    } else if (rootNode.isTopicMaximized()) { // топик не свернутым cut, содежимое в div
      return "<div id=\"cut" + Integer.toString(rootNode.getCutCount()) + "\">" + node.renderChildrenXHtml() + "</div>";
    } else if(rootNode.isTopicMinimized()) { // топик со свернутым cut, вместо содержимого ссылка
      URI uri = rootNode.getCutURI();
      try {
        uri.setFragment("cut"+Integer.toString(rootNode.getCutCount()));
        if (!node.getParameter().isEmpty()) {
          ToHtmlFormatter formatter = rootNode.getToHtmlFormatter();
          String parameter;
          if(formatter != null) {
            parameter = rootNode.getToHtmlFormatter().simpleFormat(node.getParameter().replaceAll("\"", ""));
          } else {
             parameter = Parser.escape(node.getParameter().replaceAll("\"", ""));
          }
          return String.format("<p>( <a href=\"%s\">%s</a> )</p>", uri.getEscapedURIReference(), parameter);
        } else {
          return String.format("<p>( <a href=\"%s\">читать дальше...</a> )</p>", uri.getEscapedURIReference());
        }
      } catch (Exception e) {
        return node.renderChildrenXHtml();
      }
    } else {
      throw new RuntimeException("BUG");
    }
  }
}
