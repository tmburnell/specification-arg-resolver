/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.persistence.criteria.JoinType;

import net.kaczmarzyk.spring.data.jpa.domain.Conjunction;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Joins;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.ReflectionUtils;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;


public class SpecificationArgumentResolverTest extends ResolverTestBase {

    SpecificationArgumentResolver resolver = new SpecificationArgumentResolver();
    
    @Test
    public void resolvesJoinFetchForSimpleSpec() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isInstanceOf(Conjunction.class);
        
        Collection<Specification<?>> innerSpecs = ReflectionUtils.get(resolved, "innerSpecs");
        
        assertThat(innerSpecs)
            .hasSize(2)
            .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
            .contains(new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(new String[] { "fetch1", "fetch2" }, JoinType.LEFT));
    }
    
//    @Test
//    public void resolvesRepeatedFetchJoins() throws Exception {
//    	MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod_repeatedFetch"), 0);
//        NativeWebRequest req = mock(NativeWebRequest.class);
//        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
//
//        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);
//
//        assertThat(resolved)
//            .isInstanceOf(Conjunction.class);
//        
//        Collection<Specification<?>> innerSpecs = ReflectionUtils.get(resolved, "innerSpecs");
//        
//        assertThat(innerSpecs)
//            .hasSize(2)
//            .contains(new Like<Object>("path1", new String[] { "value1" }))
//            .contains(new Conjunction<Object>(
//            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(new String[] { "fetch1" }, JoinType.LEFT),
//            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(new String[] { "fetch2" }, JoinType.INNER)));
//    }
    
    @Test
    public void resolvesJoinContainer() throws Exception {
    	MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod_joinContainer"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        QueryContext queryCtx = new WebRequestQueryContext(req);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isInstanceOf(Conjunction.class);
        
        Collection<Specification<?>> innerSpecs = ReflectionUtils.get(resolved, "innerSpecs");
        
        assertThat(innerSpecs)
            .hasSize(2)
            .contains(new Like<Object>(queryCtx, "path1", new String[] { "value1" }))
            .contains(new Conjunction<Object>(
            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(new String[] { "fetch1" }, JoinType.LEFT),
            		new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(new String[] { "fetch2" }, JoinType.INNER)));
    }
    
    @Test
    public void resolvesJoinFetchEvenIfOtherSpecificationIsNotPresent() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethod"), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isEqualTo(new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<Object>(new String[] { "fetch1", "fetch2" }, JoinType.LEFT));
    }
    
    @Test
    public void resolvesJoinFetchForAnnotatedInterface() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethodWithCustomSpec", CustomSpec.class), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isInstanceOf(CustomSpec.class); // TODO better assertions
    }
    
//    @Test
//    public void resolvesRepeatedJoinFetchForAnnotatedInterface() throws Exception {
//        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethodWithCustomSpec_repeatedFetch", CustomSpecRepeatedFetch.class), 0);
//        NativeWebRequest req = mock(NativeWebRequest.class);
//        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });
//
//        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);
//
//        assertThat(resolved)
//            .isInstanceOf(CustomSpecRepeatedFetch.class); // TODO better assertions
//    }
    
    @Test
    public void resolvesJoinContainerForAnnotatedInterface() throws Exception {
        MethodParameter param = MethodParameter.forMethodOrConstructor(testMethod("testMethodWithCustomSpec_joinContainer", CustomSpecJoinContainer.class), 0);
        NativeWebRequest req = mock(NativeWebRequest.class);
        when(req.getParameterValues("path1")).thenReturn(new String[] { "value1" });

        Specification<?> resolved = (Specification<?>) resolver.resolveArgument(param, null, req, null);

        assertThat(resolved)
            .isInstanceOf(CustomSpecJoinContainer.class); // TODO better assertions
    }

    @Override
    protected Class<?> controllerClass() {
        return TestController.class;
    }
    
    @JoinFetch(paths = { "fetch1", "fetch2" })
    @Spec(path = "path1", spec = Like.class)
    public static interface CustomSpec extends Specification<Object> {
    }
    
//    @JoinFetch(paths = { "fetch1" })
//    @JoinFetch(paths = { "fetch2" })
//    @Spec(path = "path1", spec = Like.class)
//    public static interface CustomSpecRepeatedFetch extends Specification<Object> {
//    }
    
    @Joins({
    	@JoinFetch(paths = { "fetch1" }),
        @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
    })
    @Spec(path = "path1", spec = Like.class)
    public static interface CustomSpecJoinContainer extends Specification<Object> {
    }
    
    public static class TestController {
        
    	public void testMethodWithCustomSpec(CustomSpec spec) {
        }
    	
//    	public void testMethodWithCustomSpec_repeatedFetch(CustomSpecRepeatedFetch spec) {
//        }
    	
    	public void testMethodWithCustomSpec_joinContainer(CustomSpecJoinContainer spec) {
        }
        
        public void testMethod(
                @JoinFetch(paths = { "fetch1", "fetch2" })
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
        
//        public void testMethod_repeatedFetch(
//                @JoinFetch(paths = { "fetch1" })
//                @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
//                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
//        }
        
        public void testMethod_joinContainer(
                @Joins({
                	@JoinFetch(paths = { "fetch1" }),
                    @JoinFetch(paths = { "fetch2" }, joinType = JoinType.INNER)
                })
                @Spec(path = "path1", spec = Like.class) Specification<Object> spec) {
        }
    }
}
