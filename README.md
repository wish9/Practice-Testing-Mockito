[Mockito 블로그 포스팅 주소](https://velog.io/@wish17/%EC%BD%94%EB%93%9C%EC%8A%A4%ED%85%8C%EC%9D%B4%EC%B8%A0-%EB%B0%B1%EC%97%94%EB%93%9C-%EB%B6%80%ED%8A%B8%EC%BA%A0%ED%94%84-56%EC%9D%BC%EC%B0%A8-Spring-MVC-%ED%85%8C%EC%8A%A4%ED%8C%85Testing2)

## Mockito

> 
- Mock
    - 가짜 객체 (일부 기능만 갖고 있거나 유사한 객체를 의미)
- Mocking
    - 단위 테스트나 슬라이스 테스트 등에 Mock 객체를 사용하는 것
	
> Mockito
- Mock 객체로 Mocking을 할 수 있게 해주는 라이브러리
- Mock 객체가 진짜처럼 동작하게 해줌

``@AutoConfigureMockMvc``

- MockMvc 객체를 생성해 컨트롤러의 테스트를 위한 API 테스트를 가능하게 해줌
- 스프링 부트 테스트 컨텍스트 내에 MockMvc 빈을 등록해줌
- 사용자 요청에 대해 컨트롤러를 통해 응답 결과를 확인할 수 있도록 도와줌

``@MockBean``
- Application Context에 등록되어 있는 Bean에 대한 Mockito Mock 객체를 생성하고 주입해줌
- ``@MockBean``이 붙은 객체는 해당 Bean을 Mock 객체로 대체
- ``@MockBean``은 Spring Context에 있는 Bean 중에서만 Mocking할 수 있다.

``@ExtendWith(MockitoExtension.class)``
- Spring을 사용하지 않고, Junit에서 Mockito의 기능을 사용하려면 이 애너테이션을 추가해야 함


> Stubbing
- 테스트를 위해서 Mock 객체가 항상 일정한 동작을 하도록 지정하는 것


``@Mock``
- 사용한 필드의 객체를 Mock 객체로 생성

``@InjectMocks``
- ``@Mock`` 애너테이션으로 생성한 Mock 객체를 ``@InjectMocks``를 붙인 필드 객체에 주입

***

## Mockito 실습

### 실습 1: Mockito 사용한 슬라이스(Slice) 테스트 케이스 작성

[실습1 GitHub 주소](https://github.com/wish9/Practice-Testing-Mockito/commit/6bddcfecb5750e7393073bbdec51018607d99a3b)

```java
import com.codestates.member.dto.MemberDto;
import com.codestates.member.entity.Member;
import com.codestates.member.mapper.MemberMapper;
import com.codestates.member.service.MemberService;
import com.codestates.stamp.Stamp;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;
    @MockBean // Mock 객체만들어서 주입해 줌 (Mocking할 수 있게 해줌)
    private MemberService memberService;
    @Autowired
    private MemberMapper mapper;

    @Test
    void postMemberTest() throws Exception {
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");

        Member member = mapper.memberPostToMember(post);
        member.setMemberId(1L);

        given(memberService.createMember(Mockito.any(Member.class))) // Mock객체의 createMember()메서드에 매개변수로 any Member객체가 들어가면
                .willReturn(member); // member 객체를 리턴하게 만드는 것

        String content = gson.toJson(post);

        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

    @Test
    void patchMemberTest() throws Exception {
        MemberDto.Patch patch = new MemberDto.Patch(1L, "홍길동", "010-1234-5678", Member.MemberStatus.MEMBER_ACTIVE);

        Member member = mapper.memberPatchToMember(patch);
        member.setEmail("hgd@gmail.com");
        member.setStamp(new Stamp());
        System.out.println("member = " + member);

        given(memberService.updateMember(Mockito.any(Member.class)))
                .willReturn(member);

        String patchContent = gson.toJson(patch);

        mockMvc.perform(
                patch("/v11/members/"+member.getMemberId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchContent)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.data.name").value(patch.getName()),
                jsonPath("$.data.phone").value(patch.getPhone())
                );


    }

    @Test
    void getMemberTest() throws Exception {
        Member member = makeMember();

        given(memberService.findMember(Mockito.anyLong()))
                .willReturn(member);

        mockMvc.perform(
                get("/v11/members/"+member.getMemberId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.data.name").value(member.getName()),
                jsonPath("$.data.phone").value(member.getPhone())
        );

    }

    @Test
    void getMembersTest() throws Exception {
        Member member1 = makeMember();
        Member member2 = makeMember2();

        int page = 1;
        int size = 10;

        Page<Member> list = new PageImpl<>(List.of(member1,member2), PageRequest.of(page,size, Sort.by("memberId").descending()),2);

        given(memberService.findMembers(Mockito.anyInt(),Mockito.anyInt()))
                .willReturn(list);

        mockMvc.perform(
                get("/v11/members?page="+page+"&size="+size)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(
                        status().isOk(),
                        jsonPath("$.data[0].email").value(member1.getEmail()), // 페이지네이션 정렬에 따른 순서 주의
                        jsonPath("$.data[1].email").value(member2.getEmail())
                );
    }

    @Test
    void deleteMemberTest() throws Exception { // 결론적으로 spring연결 확인하는 정도 밖에 의미 없음

        doNothing().when(memberService).deleteMember(Mockito.anyLong()); // 그나마 하나 있는 기능 아무것도 안하게 만들기

        mockMvc.perform(
                delete("/v11/members/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    public Member makeMember(){
        Member member = new Member("hgd@gmail.com", "홍길동", "010-1234-5678");
        member.setStamp(new Stamp());
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setMemberId(1L);

        return member;
    }

    public Member makeMember2(){
        Member member = new Member("hgd2@gmail.com", "둘길동", "010-2222-2222");
        member.setStamp(new Stamp());
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setMemberId(2L);

        return member;
    }
}

```

- 내부 로직에서 매개변수로 사용될 값들은 가짜(moke)객체를 만들 때 set해줘야 한다. (사용 안되는 요소들은 null이여도 상관x)


### 실습 2: Mockito사용, Spring x - cancelOrder() 테스트 케이스 작성

[풀코드 GitHub주소](https://github.com/wish9/Practice-Testing-Mockito/commit/fcd587e7f6f63ccca6858d5df0e1eadf7472c4bc)


```java
import com.codestates.exception.BusinessLogicException;
import com.codestates.order.entity.Order;
import com.codestates.order.repository.OrderRepository;
import com.codestates.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderServiceHomeworkTest {

    @Mock
    private OrderRepository orderRepository; // 테스트용 OrderRepository객체 생성
    @InjectMocks // 위에서 만든 객체 주입
    private OrderService orderService;

    @Test
    public void cancelOrderTest() {
        Order order = new Order();
        order.setOrderStatus(Order.OrderStatus.ORDER_COMPLETE);
        order.setOrderId(1L);

        given(orderRepository.findById(Mockito.anyLong()))
                .willReturn(Optional.of(order));

        assertThrows(BusinessLogicException.class, ()->orderService.cancelOrder(order.getOrderId()));
    }
}

```

#### 실습내용 외에 다양한 시도에서 알게된 사실
- ``assertEquals()`` 메서드가 때때로 주소값을 비교하기 때문에 값을 비교하고 싶으면 습관적으로 toSting을 각 매개값에 붙여줘야 한다.
