package io.github.yeongseoksong.demo.controller;

import io.github.yeongseoksong.platform.cqrs.command.CommandBus;
import io.github.yeongseoksong.platform.cqrs.query.QueryBus;
import io.github.yeongseoksong.platform.exception.BusinessException;
import io.github.yeongseoksong.platform.response.ApiResponse;
import io.github.yeongseoksong.demo.exception.DemoErrorCode;
import io.github.yeongseoksong.demo.greeting.command.CreateGreetingCommand;
import io.github.yeongseoksong.demo.greeting.query.GreetingQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
public class HelloController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    // Command: 상태 변경 (쓰기)
    @PostMapping
    public ApiResponse<String> create(@RequestBody @Valid CreateRequest request) {

        String result = commandBus.dispatch(new CreateGreetingCommand(request.getName()));
        return ApiResponse.success(result);
    }

    // Query: 조회 (읽기)
    @GetMapping("/{name}")
    public ApiResponse<String> get(@PathVariable String name) {
        if (name.equals("error")) {
            throw new BusinessException(DemoErrorCode.HELLO_NOT_FOUND);
        }
        String result = queryBus.dispatch(new GreetingQuery(name));
        return ApiResponse.success(result);
    }

    @Getter
    static class CreateRequest {
        @NotBlank(message = "name must not be blank!")
        private String name;
    }
}
