import { GET, POST, PUT, DELETE }  from "./fetch-action";

interface PostArticle {
  id? : string,
  title: string,
  content: string
}

const createTokenHeader = (token:string) => {
  const URL = ''
  return {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    }
  }
}

export const getArticleList = () => {
  const URL = '/api/user/article/all/';
  const response = GET(URL, {});
  return response;
};

export const getPageList = (param:string) => {
  const URL = '/api/user/article/page?page=' + param;
  const response = GET(URL, {});
  return response;
}

export const getOneArticle = (param:string, token?:string) => {
  const URL= '/api/user/article/one?id=' + param;
  if (!token) {
    const response = GET(URL, {});
    return response;
  } else {
    const response = GET(URL, createTokenHeader(token));
    return response;
  }
};

export const makeArticle = (token:string, article:PostArticle) => {
  const URL = '/api/user/article/write';
  const response = POST(URL, article, createTokenHeader(token));
  return response;
};

export const getUpdateArticle = (token:string, param:string) => {
  const URL = '/api/user/article/update?id=' + param;
  const response = GET(URL, createTokenHeader(token));
  return response;
};

export const updateArticle = (token:string, article:PostArticle) => {
  const URL = '/api/user/article/updateOK';
  const response = PUT(URL, article, createTokenHeader(token));
  // console.log(article.id); - 여기까지 id 잘 옴!
  return response;
};

export const deleteArticle = (token:string, param:string) => {
  const URL = '/api/user/article/one?id=' + param;
  const response = DELETE(URL, createTokenHeader(token));
  return response;
}