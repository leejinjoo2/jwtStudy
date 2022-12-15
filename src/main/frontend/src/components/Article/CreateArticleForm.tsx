import React, { useCallback, useContext, useEffect, useRef, useState } from "react";
import { Button, Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import ArticleContext from "../../store/article-context";
import AuthContext from "../../store/auth-context";
import { PostArticle } from "../../utility/types"

type Props = { item:string | undefined }

const CreateArticleForm:React.FC<Props> = (props) => {

  let navigate = useNavigate();

  const [updateArticle, setUpdateArticle] = useState<PostArticle>({
    title: '',
    content: ''
  });

  const articleCtx = useContext(ArticleContext);
  const authCtx = useContext(AuthContext);

  const titleRef = useRef<HTMLInputElement>(null);
  const contentRef = useRef<HTMLTextAreaElement>(null);

  const submitHandler = (event: React.FormEvent) => {
    event.preventDefault();

    let postArticle:PostArticle = {
      title: titleRef.current!.value,
      content: contentRef.current!.value
    }

    if (props.item) {
      console.log('update!');
      postArticle = { ...postArticle, id:props.item }
    }

    props.item 
    ? articleCtx.updateArticle(authCtx.token, postArticle) : articleCtx.createArticle(postArticle, authCtx.token);
  }

  const setUpdateArticleHandler = useCallback(() => {
    console.log("수정할 때 여기 옵니까")
    if (articleCtx.isGetUpdateSuccess) {
      setUpdateArticle({
        title: articleCtx.article!.title,
        content: articleCtx.article!.content
      })
    }
  }, [articleCtx.isGetUpdateSuccess])

  useEffect(() => {
    if (props.item) {
      articleCtx.getUpdateArticle(authCtx.token, props.item);
    }
  }, [props.item])

  useEffect(() => {
    console.log('update effect')
    setUpdateArticleHandler();
  }, [setUpdateArticleHandler])

  useEffect(() => {
    if (articleCtx.isSuccess) {
      console.log("writing success");
      navigate("/page/1", { replace: true })
    }
  }, [articleCtx.isSuccess])

  return (
    <div>
        <Form onSubmit={submitHandler}>
          {/*<Form.Group >*/}
          {/*  <Form.Control*/}
          {/*    defaultValue={updateArticle.id}*/}
          {/*  />*/}
          {/*</Form.Group>*/}
          <Form.Group>
            <Form.Label>제목</Form.Label>
            <Form.Control 
              type="text" 
              placeholder="제목을 입력하세요"
              required
              ref={titleRef}
              defaultValue={updateArticle.title}
            />
          </Form.Group>
          <br />
          <Form.Group>
            <Form.Label>본문</Form.Label>
            <Form.Control 
              as="textarea" 
              rows={20}
              required
              ref={contentRef}
              defaultValue={updateArticle.content} 
            /> 
          </Form.Group>
          <br />
          <Button variant="primary">
            취소
          </Button>
          <Button variant="primary" type="submit">
            작성
          </Button>
        </Form>
    </div>
  );
}

export default CreateArticleForm;